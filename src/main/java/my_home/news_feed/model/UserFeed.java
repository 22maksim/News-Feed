package my_home.news_feed.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserFeed {
    @Id
    private String id; //id юзера например id = "user:" + userId; это ключ для фида в редисе
    private List<Long> postIds;
    // В редисе это сортет сет по времени создания постов. Количество до 500шт. Далее старые удаляются

    public void addPostId(Long postId) {
        if (postIds == null) {
            postIds = new ArrayList<>();
        }
        postIds.add(postId);
    }
}
