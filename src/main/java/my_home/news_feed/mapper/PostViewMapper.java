package my_home.news_feed.mapper;

import my_home.news_feed.model.PostViews;
import my_home.news_feed.model.dto.PostViewsDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostViewMapper {
    PostViewsDto toViewDto(PostViews postViews);
}
