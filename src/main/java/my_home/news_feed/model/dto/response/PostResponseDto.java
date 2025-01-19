package my_home.news_feed.model.dto.response;

import lombok.*;
import my_home.news_feed.model.dto.CommentDto;
import my_home.news_feed.model.dto.LikeDto;
import my_home.news_feed.model.dto.PostViewsDto;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private List<CommentDto> commentDtoList;
    private List<LikeDto> likeDtoList;
    private List<PostViewsDto> postViewDtoList;
    private Instant createdAt;
    // ищим по id в редиске и начинаем собирать комменты лайки и просмотры по айди поста
}
