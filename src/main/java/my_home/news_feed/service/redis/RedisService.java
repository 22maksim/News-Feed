package my_home.news_feed.service.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_home.news_feed.feign_client.PostClientService;
import my_home.news_feed.mapper.CommentMapper;
import my_home.news_feed.mapper.LikeMapper;
import my_home.news_feed.mapper.PostViewMapper;
import my_home.news_feed.model.Comment;
import my_home.news_feed.model.Post;
import my_home.news_feed.model.dto.CommentDto;
import my_home.news_feed.model.dto.LikeDto;
import my_home.news_feed.model.dto.PostViewsDto;
import my_home.news_feed.model.dto.response.PostResponseDto;
import my_home.news_feed.model.dto.response.UserFeedResponseDto;
import my_home.news_feed.model.properties.SizeProperties;
import my_home.news_feed.repository.LikeRedisRepository;
import my_home.news_feed.repository.PostRepository;
import my_home.news_feed.repository.PostViewsRedisRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {
    private final PostRepository postRepository;
    private final LikeRedisRepository likeRedisRepository;
    private final PostViewsRedisRepository postViewsRedisRepository;
    private final PostClientService postClientService;
    private final CommentMapper commentMapper;
    private final ObjectMapper mapper;
    private final LikeMapper likeMapper;
    private final PostViewMapper postViewMapper;
    private final SizeProperties sizeProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    public void checkAndSaveComment(Comment comment) throws JsonProcessingException {
        String key = "post/" + comment.getPostId() + "/comment";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(getLuaScript(), Long.class);

        String commentJson = mapper.writeValueAsString(comment);

        redisTemplate.execute(
                redisScript,
                Collections.singletonList(key),         // ключ сета редиса для комментариев
                sizeProperties.sizeCommentSet(),        // макс размер для сета комментов
                comment.getCreatedAt().toEpochMilli(),  // по времени сортируем комментарии
                commentJson                             // сохраняем коммент
        );
        redisTemplate.expire(key, Duration.ofHours(sizeProperties.ttl()));
    }

    public void addPostInUserFeed(Long userId, Long postId, Instant createdAt) {
        String key = "user-posts:" + userId;

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(getLuaScript(), Long.class);

        redisTemplate.execute(
                redisScript,
                Collections.singletonList(key),
                sizeProperties.sizeUserFeed(), // ARGV[1] - максимальный размер множества
                createdAt.toEpochMilli(),      // ARGV[2] - score для ZADD , т.е. сортировка
                postId                         // ARGV[3] - данные для сохранения
        );
        redisTemplate.expire(key, Duration.ofHours(sizeProperties.ttl()));
    }


    public UserFeedResponseDto collectUserFeed(Long userId, int page, int size) {
        String key = "user-posts:" + userId;

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
        String keyComments = "post/" + post.getId() + "/comment";
        List<CommentDto> commentsDtoList = getCommentsDtoFromRedis(keyComments);
        List<LikeDto> likesDtoList = getLikeFromRedis(post.getId());
        List<PostViewsDto> viewsDtoList = getPostViewsFromRedis(post.getId());

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
        List<Long> postIds;

        try {
            postIds = Objects.requireNonNull(redisTemplate.opsForZSet().reverseRange(key, start, end))
                    .stream()
                    .map(obj -> Long.getLong(obj.toString()))
                    .toList();
        } catch (NullPointerException ex) {
            postIds = postClientService.getPosts().entrySet().stream()
                    .sorted((entry1, entry2) ->
                            Long.compare(entry2.getValue().toEpochMilli(), entry1.getValue().toEpochMilli()))
                    .map(Map.Entry::getKey)
                    .toList();
            log.error("Error fetching post IDs from Redis for key: {}", key, ex);
        }
        return postIds;
    }

    private List<CommentDto> getCommentsDtoFromRedis(String key) {
        return Optional.ofNullable(redisTemplate.opsForZSet().reverseRange(key, 0, -1))
                .orElse(Collections.emptySet())
                .stream()
                .filter(Objects::nonNull)
                .map(str -> mapper.convertValue(str, Comment.class))
                .map(commentMapper::toCommentDto)
                .toList();
    }

    private List<LikeDto> getLikeFromRedis(Long postID) {
        return likeRedisRepository.findAllByPostId(postID).stream()
                .filter(Objects::nonNull)
                .map(likeMapper::toLikeDto).toList();
    }

    private List<PostViewsDto> getPostViewsFromRedis(Long postID) {
        return postViewsRedisRepository.findAllByPostId(postID).stream()
                .filter(Objects::nonNull)
                .map(postViewMapper::toViewDto).toList();
    }

    private String getLuaScript() {
        return "if redis.call('zcard', KEYS[1]) >= tonumber(ARGV[1]) then " +
                "  redis.call('zremrangebyrank', KEYS[1], 0, 0) " +
                "end " +
                "redis.call('zadd', KEYS[1], ARGV[2], ARGV[3])";
    }
}
