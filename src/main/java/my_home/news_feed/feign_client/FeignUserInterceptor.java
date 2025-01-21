package my_home.news_feed.feign_client;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import my_home.news_feed.config.context.UserContext;

@RequiredArgsConstructor
public class FeignUserInterceptor implements RequestInterceptor {

    private final UserContext userContext;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("x-user-id", String.valueOf(userContext.getUserId()));
    }
}
