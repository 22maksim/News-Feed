package my_home.news_feed.model.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedResponseDto {
    private Long userId;
    private List<PostResponseDto> posts;
}
