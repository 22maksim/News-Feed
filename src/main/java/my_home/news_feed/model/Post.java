package my_home.news_feed.model;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("posts")
public class Post {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorSubscriberIdsJson;
    private Instant createdAt;
}
