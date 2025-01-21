package my_home.news_feed.repository;

import my_home.news_feed.model.Author;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRedisRepository extends CrudRepository<Author, Long> {
    List<Author> getAuthorById(Long id);
}
