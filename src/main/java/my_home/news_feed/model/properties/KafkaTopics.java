package my_home.news_feed.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("topic.kafka")
public record KafkaTopics(
        String likes,
        String posts,
        String  postViews,
        String comments
) {
}