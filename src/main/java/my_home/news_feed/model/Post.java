package my_home.news_feed.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Builder
@RedisHash(value = "posts")
@NoArgsConstructor
@AllArgsConstructor
public class Post implements Serializable {
    @Id
    private Long id;

    private String title;
    private String content;

    @Indexed
    private Long authorId;

    private String authorSubscriberIdsJson;
    private Instant createdAt;

    @TimeToLive
    private Long ttl;
}