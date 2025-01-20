package my_home.news_feed.model.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateEvent implements Serializable {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorSubscriberIdsJson;
    private Instant createdAt;
}