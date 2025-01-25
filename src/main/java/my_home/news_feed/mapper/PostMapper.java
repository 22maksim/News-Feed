package my_home.news_feed.mapper;

import my_home.news_feed.model.Post;
import my_home.news_feed.model.event.PostCreateEvent;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    Post toEntity(PostCreateEvent event);
}
