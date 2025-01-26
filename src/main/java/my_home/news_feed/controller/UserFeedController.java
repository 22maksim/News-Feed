package my_home.news_feed.controller;

import lombok.RequiredArgsConstructor;
import my_home.news_feed.config.context.UserContext;
import my_home.news_feed.model.dto.response.UserFeedResponseDto;
import my_home.news_feed.service.feed.UserFeedService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("api/v1/user-feed/")
@RequiredArgsConstructor
public class UserFeedController {
    private final UserFeedService userFeedServiceImpl;
    private final UserContext userContext;

    @GetMapping
    public UserFeedResponseDto getUserFeed(@RequestParam(name = "page", defaultValue = "1") int page,
                                           @RequestParam(name = "size", defaultValue = "20") int size) {
        Long userId = userContext.getUserId();
        return userFeedServiceImpl.getUserFeed(userId, page, size);
    }
}
