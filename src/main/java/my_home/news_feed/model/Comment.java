package my_home.news_feed.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "comment")
public class Comment implements Serializable {
    @Id
    private Long id;
    private String comment;
    private Long author_id;
    private Long postId;
    private Instant createdAt;

    @TimeToLive
    private Long ttl;
}