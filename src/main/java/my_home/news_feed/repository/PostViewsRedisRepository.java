package my_home.news_feed.repository;

import my_home.news_feed.model.PostViews;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostViewsRedisRepository extends CrudRepository<PostViews, Long> {
    List<PostViews> findAllByPostId(Long postId);
}
