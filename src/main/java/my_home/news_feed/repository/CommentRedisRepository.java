package my_home.news_feed.repository;

import my_home.news_feed.model.Comment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRedisRepository extends CrudRepository<Comment, Long> {
    List<Comment> findAllByPostId(Long postId);
}
