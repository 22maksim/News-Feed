package my_home.news_feed.mapper;

import my_home.news_feed.model.Like;
import my_home.news_feed.model.dto.LikeDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LikeMapper {
    LikeDto toLikeDto(Like like);
}
