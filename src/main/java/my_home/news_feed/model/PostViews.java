package my_home.news_feed.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "post_views")
public class PostViews implements Serializable {
    @Id
    private Long id;
    private Long postId;
    private Long userId;
    private Instant timeView;

    @TimeToLive
    private Long ttl;
}