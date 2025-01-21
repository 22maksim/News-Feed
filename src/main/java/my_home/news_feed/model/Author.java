package my_home.news_feed.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "authors")
public class Author {
    @Id
    private Long id;

    @Indexed
    private String username; // получить через FeignClient
}
