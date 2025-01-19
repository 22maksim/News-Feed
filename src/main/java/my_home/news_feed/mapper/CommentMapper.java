package my_home.news_feed.mapper;

import my_home.news_feed.model.Comment;
import my_home.news_feed.model.dto.CommentDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    CommentDto toCommentDto(Comment comment);
}
