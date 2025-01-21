package my_home.news_feed.repository;

import my_home.news_feed.model.PostViews;
import my_home.news_feed.model.dto.PostViewsDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.StreamSupport;

@Repository
public interface PostViewsRedisRepository extends CrudRepository<PostViews, String> {
    List<PostViews> findAllByPostId(Long postId);
}
