package my_home.news_feed.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "topic.kafka")
public record KafkaTopics(
        String likes,
        String postCreate,
        String  postViews,
        String comments
) {
}