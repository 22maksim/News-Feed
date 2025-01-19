package my_home.news_feed.service.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_home.news_feed.mapper.CommentMapper;
import my_home.news_feed.mapper.LikeMapper;
import my_home.news_feed.mapper.PostViewMapper;
import my_home.news_feed.model.Post;
import my_home.news_feed.model.dto.CommentDto;
import my_home.news_feed.model.dto.LikeDto;
import my_home.news_feed.model.dto.PostViewsDto;
import my_home.news_feed.model.dto.response.PostResponseDto;
import my_home.news_feed.model.dto.response.UserFeedResponseDto;
import my_home.news_feed.model.properties.SizeProperties;
import my_home.news_feed.repository.CommentRedisRepository;
import my_home.news_feed.repository.LikeRedisRepository;
import my_home.news_feed.repository.PostRepository;
import my_home.news_feed.repository.PostViewsRedisRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {
    private final PostRepository postRepository;
    private final CommentRedisRepository commentRedisRepository;
    private final LikeRedisRepository likeRedisRepository;
    private final PostViewsRedisRepository postViewsRedisRepository;
    private final CommentMapper commentMapper;
    private final LikeMapper likeMapper;
    private final PostViewMapper postViewMapper;
    private final SizeProperties sizeProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    public void addPostUserFeed(Long userId, Long postId, Instant createdAt) {
        String key = "user:" + userId;
        String luaScript =
                "if redis.call('zcard', KEYS[1]) >= tonumber(ARGV[1]) then " +
                        "  redis.call('zremrangebyrank', KEYS[1], 0, 0) " +
                        "end " +
                        "redis.call('zadd', KEYS[1], ARGV[2], ARGV[3])";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        redisTemplate.execute(
                redisScript,
                Collections.singletonList(key),
                sizeProperties.sizeUserFeed(), // ARGV[1] - максимальный размер множества
                createdAt.toEpochMilli(),      // ARGV[2] - score для ZADD
                postId                         // ARGV[3] - ID поста
        );
    }


    public UserFeedResponseDto findUserFeed(Long userId, int page, int size) {
        String key = "user:" + userId;

        List<Long> postIds = getPostIdsFromRedis(key, page, size);

        List<PostResponseDto> postsResponse = StreamSupport.stream(postRepository.findAllById(postIds).spliterator(), true)
                .filter(Objects::nonNull)
                .map(this::buildPostResponseDto)
                .toList();

        return UserFeedResponseDto.builder()
                .userId(userId)
                .posts(postsResponse)
                .build();
    }

    private PostResponseDto buildPostResponseDto(Post post) {
        List<CommentDto> commentsDtoList = commentRedisRepository.findAllByPostId(post.getId()).stream()
                .filter(Objects::nonNull)
                .map(commentMapper::toCommentDto).toList();
        List<LikeDto> likesDtoList = likeRedisRepository.findAllByPostId(post.getId()).stream()
                .filter(Objects::nonNull)
                .map(likeMapper::toLikeDto).toList();
        List<PostViewsDto> viewsDtoList = postViewsRedisRepository.findAllByPostId(post.getId()).stream()
                .filter(Objects::nonNull)
                .map(postViewMapper::toViewDto).toList();

        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorId(post.getAuthorId())
                .commentDtoList(commentsDtoList)
                .likeDtoList(likesDtoList)
                .postViewDtoList(viewsDtoList)
                .build();
    }

    private List<Long> getPostIdsFromRedis(String key, int page, int size) {
        long start = (long) page * size;
        long end = start + size - 1;
        List<Long> postIds = List.of();
        try {
            postIds = Objects.requireNonNull(redisTemplate.opsForZSet().reverseRange(key, start, end)).stream()
                    .map(obj -> Long.getLong(obj.toString()))
                    .toList();
        } catch (NullPointerException ex) {

            // Подумай как достать данные если их нет в бд. Это получается если у пользователя за последние сутки
            // не произошло ничего с подписками, придется идти в бд и доставать последние 500 и закидывать их в редис

            log.error("Error fetching post IDs from Redis for key: {}", key, ex);
        }
        return postIds;
    }

}
