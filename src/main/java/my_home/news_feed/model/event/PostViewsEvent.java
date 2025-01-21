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
public class PostViewsEvent implements Serializable {
    private Long postId;
    private Long userId;
    private Instant timeView;
}
