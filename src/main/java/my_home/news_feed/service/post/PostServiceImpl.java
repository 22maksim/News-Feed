package my_home.news_feed.service.post;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_home.news_feed.exception.AuthorException;
import my_home.news_feed.feign_client.UserClientService;
import my_home.news_feed.model.Author;
import my_home.news_feed.model.Comment;
import my_home.news_feed.model.Post;
import my_home.news_feed.model.dto.UserAuthorDto;
import my_home.news_feed.repository.AuthorRedisRepository;
import my_home.news_feed.repository.CommentRedisRepository;
import my_home.news_feed.repository.LikeRedisRepository;
import my_home.news_feed.repository.PostRepository;
import my_home.news_feed.service.redis.RedisService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
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

    @Async
    @Override
    public CompletableFuture<Void> eventCreatePost(Long userID, Post post) {
        postRepository.save(post);
        redisService.addPostUserFeed(userID, post.getId(), post.getCreatedAt());
        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Override
    public CompletableFuture<Void> eventCommentForPost(Comment comment) {
        Author author = getAndSaveAuthor(comment);
        List<Comment> allComments = commentRedisRepository.findAllByPostId(comment.getPostId());

        if (allComments.isEmpty() || allComments.size() < 3) {
            commentRedisRepository.save(comment);
            return CompletableFuture.completedFuture(null);
        } else {
            removeNecessaryComments(allComments);
        }

        return CompletableFuture.completedFuture(null);
    }

    private Author getAndSaveAuthor(Comment comment) {
        if (!authorRedisRepository.existsById(comment.getAuthor_id())) {
            UserAuthorDto authorDto = userClientService.getAuthor(comment.getAuthor_id());
            return Author.builder().id(authorDto.getId()).username(authorDto.getUsername()).build();
        } else {
            return authorRedisRepository.findById(comment.getAuthor_id())
                    .orElseThrow(() -> new AuthorException("Author not found"));
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
}
