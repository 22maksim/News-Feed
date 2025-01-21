package my_home.news_feed.service.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_home.news_feed.exception.AuthorException;
import my_home.news_feed.feign_client.UserClientService;
import my_home.news_feed.model.Author;
import my_home.news_feed.model.Comment;
import my_home.news_feed.model.Like;
import my_home.news_feed.model.Post;
import my_home.news_feed.model.event.PostLikeEvent;
import my_home.news_feed.model.properties.SizeProperties;
import my_home.news_feed.repository.AuthorRedisRepository;
import my_home.news_feed.repository.CommentRedisRepository;
import my_home.news_feed.repository.LikeRedisRepository;
import my_home.news_feed.repository.PostRepository;
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
    private final LikeRedisRepository likeRedisRepository;
    private final AuthorRedisRepository authorRedisRepository;
    private final PostRepository postRepository;
    private final RedisService redisService;
    private final UserClientService userClientService;
    private final CommentRedisRepository commentRedisRepository;
    private final SizeProperties sizeProperties;
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
        if (likeRedisRepository.existsById(event.getPostId() + "_" + event.getUserId())) {
            return CompletableFuture.completedFuture(null);
        }
        if (!authorRedisRepository.existsById(event.getUserId())) {
            getAndSaveAuthor(event.getUserId());
        }
        Like like = Like.builder()
                .postId(event.getPostId())
                .userId(event.getUserId())
                .ttl(Duration.ofHours(sizeProperties.ttl()).toMillis())
                .build();
        like.setKeyFromRedis();
        likeRedisRepository.save(like);

        return CompletableFuture.completedFuture(null);
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
        List<Long> deleteCommentsId = allComments.stream()
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
