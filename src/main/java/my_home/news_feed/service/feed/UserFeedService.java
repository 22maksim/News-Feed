package my_home.news_feed.service.feed;

import my_home.news_feed.model.dto.response.UserFeedResponseDto;

public interface UserFeedService {

    UserFeedResponseDto getUserFeed(Long userId, int page, int size);
}
