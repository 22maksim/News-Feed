package my_home.news_feed.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Getter
@Setter
@RedisHash("Event")
@NoArgsConstructor
@AllArgsConstructor
public class Event implements Serializable {
    private String id;
    private String title;
    private String description;
}

