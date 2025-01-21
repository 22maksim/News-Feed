package my_home.news_feed.feign_client;

import my_home.news_feed.config.context.UserContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    FeignUserInterceptor feignUserInterceptor(UserContext userContext) {
        return new FeignUserInterceptor(userContext);
    }
}
