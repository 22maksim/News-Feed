package my_home.news_feed.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "like")
public class Like implements Serializable {
    @Id
    private Long id;

    @Indexed
    private Long postId;

    @Indexed
    private Long userId;

    @TimeToLive
    private Long ttl;
}