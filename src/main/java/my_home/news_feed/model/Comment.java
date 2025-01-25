package my_home.news_feed.model;

import jakarta.validation.constraints.NotNull;
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
public class Comment implements Serializable {
    @NotNull
    private String comment;

    @NotNull
    private Long authorId;

    @NotNull
    private Long postId;

    @NotNull
    private Instant createdAt;
}