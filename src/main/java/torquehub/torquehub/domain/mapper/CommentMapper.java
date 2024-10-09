package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.model.Comment;
import torquehub.torquehub.domain.request.CommentDtos.CommentCreateRequest;
import torquehub.torquehub.domain.request.CommentDtos.CommentEditRequest;
import torquehub.torquehub.domain.response.CommentDtos.CommentResponse;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    Comment toEntity(CommentCreateRequest commentCreateRequest);
    Comment toEntity(CommentEditRequest commentEditRequest);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "postedTime", source = "commentedTime")
    @Mapping(target = "userPoints", source = "user.points")
    CommentResponse toResponse(Comment comment);
}
