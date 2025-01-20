package my_home.news_feed.model.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private  long id;
    private String title;
    private String content;
    private Long authorId;
    private List<String> comments;
    private int likes;
    private Instant createdAt;
}
