package my_home.news_feed.repository;

import my_home.news_feed.model.Like;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeRedisRepository extends CrudRepository<Like, Long> {
    List<Like> findAllByPostId(Long postId);
}
