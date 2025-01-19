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
public class PostCommentEvent implements Serializable {
    private String comment;
    private Long author_id;
    private Long postId;
    private Instant createdAt;
}
