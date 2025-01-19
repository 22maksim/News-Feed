package my_home.news_feed.service.post;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_home.news_feed.model.Post;
import my_home.news_feed.repository.LikeRedisRepository;
import my_home.news_feed.repository.PostRepository;
import my_home.news_feed.service.redis.RedisService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final LikeRedisRepository eventRedisRepository;
    private final PostRepository postRepository;
    private final RedisService redisService;

    @Async
    @Override
    public CompletableFuture<Void> eventCreatePost(Long userID, Post post) {
        postRepository.save(post);
        redisService.addPostUserFeed(userID, post.getId(), post.getCreatedAt());
        return CompletableFuture.completedFuture(null);
    }


}
