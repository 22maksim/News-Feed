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
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "comment")
public class Comment implements Serializable {
    @Id
    private String id;

    private String comment;

    @Indexed
    private Long author_id;

    @Indexed
    private Long postId;

    private Instant createdAt;

    @TimeToLive
    private Long ttl;

    public void setKeyFromRedis() {
        this.id = this.author_id + "_" + this.postId;
    }
}