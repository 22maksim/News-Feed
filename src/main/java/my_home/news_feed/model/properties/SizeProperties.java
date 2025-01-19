package my_home.news_feed.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "properties-size")
public record SizeProperties(
        int sizeUserFeed
) {
}
