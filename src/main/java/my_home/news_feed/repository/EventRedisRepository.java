package my_home.news_feed.repository;

import my_home.news_feed.model.Event;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRedisRepository extends CrudRepository<Event, Long> {
}
