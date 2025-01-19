package my_home.news_feed.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("comment")
public class Comment {
    @Id
    private Long id;
    private String comment;
    private Long author_id;
    private Long postId;
    private Instant createdAt;
}
