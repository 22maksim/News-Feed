package my_home.news_feed.service.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_home.news_feed.exception.AuthorException;
import my_home.news_feed.feign_client.UserClientService;
import my_home.news_feed.model.*;
import my_home.news_feed.model.event.PostLikeEvent;
import my_home.news_feed.model.event.PostViewsEvent;
import my_home.news_feed.model.properties.SizeProperties;
import my_home.news_feed.repository.*;
import my_home.news_feed.service.redis.RedisService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final LikeRedisRepository likeRedisRepository;
    private final AuthorRedisRepository authorRedisRepository;
    private final CommentRedisRepository commentRedisRepository;
    private final PostViewsRedisRepository postViewsRedisRepository;
    private final UserClientService userClientService;
    private final SizeProperties sizeProperties;
    private final RedisService redisService;
    private final ObjectMapper mapper;

    @Async
    @Override
    public CompletableFuture<Void> eventCreatePost(Long userID, Post post) {
        if (!authorRedisRepository.existsById(userID)) {
            HashMap<String, Long> hashUser = userClientService.getAuthor(userID);
            Map.Entry<String, Long> entry = hashUser.entrySet().iterator().next();
            Author author = Author.builder().id(entry.getValue()).username(entry.getKey()).build();
            authorRedisRepository.save(author);
        }
        try {
            List<Long> userSubscribersId = mapper.readValue(post.getAuthorSubscriberIdsJson(), new TypeReference<>() {
            });
            getAndSaveAuthors(userSubscribersId);
        } catch (JsonProcessingException ex) {
            log.error("Json problem: {}, ex {}", post.getAuthorSubscriberIdsJson(), ex.toString());
        }

        postRepository.save(post);
        redisService.addPostUserFeed(userID, post.getId(), post.getCreatedAt());
        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Override
    public CompletableFuture<Void> eventCommentForPost(Comment comment) {
        if (commentRedisRepository.existsById(comment.getId())) {
            log.info("Comment with id {} is exists", comment.getId());
            return CompletableFuture.completedFuture(null);
        }
        validateEventComment(comment);
        getAndSaveAuthor(comment.getAuthor_id());
        List<Comment> allComments = commentRedisRepository.findAllByPostId(comment.getPostId());

        if (allComments.isEmpty() || allComments.size() < 3) {
            commentRedisRepository.save(comment);
            return CompletableFuture.completedFuture(null);
        } else {
            removeNecessaryComments(allComments);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Override
    public CompletableFuture<Void> eventLikePost(PostLikeEvent event) {
        if (likeRedisRepository.existsById(event.getUserId() + "_" + event.getPostId())) {
            return CompletableFuture.completedFuture(null);
        }
        validateEventLike(event);
        getAndSaveAuthor(event.getUserId());
        Like like = Like.builder()
                .postId(event.getPostId())
                .userId(event.getUserId())
                .ttl(Duration.ofHours(sizeProperties.ttl()).toMillis())
                .build();
        like.setKeyFromRedis();
        likeRedisRepository.save(like);

        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Override
    public CompletableFuture<Void> eventViewPost(PostViewsEvent event) {
        if (postViewsRedisRepository.existsById(event.getUserId() + "_" + event.getPostId())) {
            log.info("Post views for {}, is exists", event.getPostId());
            return CompletableFuture.completedFuture(null);
        }
        validateEventView(event);
        getAndSaveAuthor(event.getUserId());

        PostViews view = PostViews.builder()
                .postId(event.getPostId())
                .userId(event.getUserId())
                .ttl(Duration.ofHours(sizeProperties.ttl()).toMillis())
                .build();
        view.setIdFromRedis();
        postViewsRedisRepository.save(view);
        return CompletableFuture.completedFuture(null);
    }

    private void validateEventView(PostViewsEvent event) {
        if (event.getPostId() == null || event.getUserId() == null) {
            log.error("Invalid event view");
            throw new AuthorException("You need to specify postId and userId");
        }
    }

    private void validateEventLike(PostLikeEvent event) {
        if (event.getPostId() == null || event.getUserId() == null) {
            log.error("Invalid event like");
            throw new AuthorException("You need to specify postId and userId");
        }
    }

    private void validateEventComment(Comment comment) {
        if (comment.getComment() == null || comment.getComment().isEmpty() || comment.getAuthor_id() == null
                || comment.getPostId() == null) {
            log.error("Comment with id {} is not valid", comment.getId());
            throw new AuthorException("Comment with id " + comment.getId() + " is not valid");
        }
    }

    private void getAndSaveAuthor(Long userID) {
        if (!authorRedisRepository.existsById(userID)) {
            HashMap<String, Long> hashUser = userClientService.getAuthor(userID);
            if (hashUser == null || hashUser.size() != 1) {
                log.info("Author id: {} is not found", userID);
                throw new AuthorException("Author id " + userID + " is not found");
            }
            Map.Entry<String, Long> entry = hashUser.entrySet().iterator().next();

            Author author = Author.builder()
                    .id(entry.getValue())
                    .username(entry.getKey())
                    .ttl(Duration.ofHours(sizeProperties.ttl()).toMillis())
                    .build();
            authorRedisRepository.save(author);
        } else {
            log.info("Author id: {} is contained in the Redis database", userID);
        }
    }

    private void removeNecessaryComments(List<Comment> allComments) {
        List<Comment> actual = allComments.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(2)
                .toList();
        List<String> deleteCommentsId = allComments.stream()
                .filter(com -> !actual.contains(com))
                .map(Comment::getId)
                .toList();
        commentRedisRepository.deleteAllById(deleteCommentsId);
    }

    private void getAndSaveAuthors(List<Long> userSubscribersId) {
        HashMap<String, Long> userHashLists = userClientService.getAuthors(userSubscribersId);

        for (Map.Entry<String, Long> entry : userHashLists.entrySet()) {
            if (entry.getValue() == null || entry.getValue() == 0) {
                continue;
            }
            if (!authorRedisRepository.existsById(entry.getValue())) {
                Author author = Author.builder().id(entry.getValue()).username(entry.getKey()).build();
                authorRedisRepository.save(author);
            }
        }
    }
}
