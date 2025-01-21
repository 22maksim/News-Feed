package my_home.news_feed.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthorDto {
    @NotNull
    private Long id;
    @NotEmpty
    private String username;
}
