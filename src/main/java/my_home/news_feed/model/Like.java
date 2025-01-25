package my_home.news_feed.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "like")
public class Like implements Serializable {
    @Id
    private String id;

    @Indexed
    private Long postId;

    @Indexed
    private Long userId;

    @TimeToLive
    private Long ttl;

    public void setKeyFromRedis() {
        this.id = this.postId + "_" + this.userId;
    }
}