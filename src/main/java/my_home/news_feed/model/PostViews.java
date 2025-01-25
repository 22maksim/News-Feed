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
@RedisHash(value = "post_views")
public class PostViews implements Serializable {
    @Id
    private String id;

    @Indexed
    private Long postId;

    private Long userId;
    private Instant timeView;

    @TimeToLive
    private Long ttl;

    public void setIdFromRedis() {
        this.id = this.userId + "_" + this.postId;
    }
}