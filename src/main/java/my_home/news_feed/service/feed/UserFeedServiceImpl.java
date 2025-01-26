package my_home.news_feed.service.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_home.news_feed.exception.UserException;
import my_home.news_feed.model.dto.response.UserFeedResponseDto;
import my_home.news_feed.service.redis.RedisService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFeedServiceImpl implements UserFeedService {
    private final RedisService redisService;

    @Override
    public UserFeedResponseDto getUserFeed(Long userId, int page, int size) {
        if (userId == null) {
            log.error("Request getUserFeed. userId is null");
            throw new UserException("UserID cannot be null");
        }
        UserFeedResponseDto responseDto = redisService.collectUserFeed(userId, page, size);

        if (responseDto == null || responseDto.getPosts().isEmpty()) {
            log.error("Cannot get user feed. UserId: {}", userId);
            throw new UserException("Cannot get user feed. UserId: " + userId);
        }
        log.info("User feed is completed. UserId: {}", userId);

        return responseDto;
    }
}
