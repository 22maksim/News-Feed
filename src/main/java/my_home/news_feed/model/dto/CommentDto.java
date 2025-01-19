package my_home.news_feed.model.dto;

import java.time.Instant;

public class CommentDto {
    private String comment;
    private Long author_id;
    private Long postId;
    private Instant createdAt;
}
