package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.model.jpa_models.JpaComment;
import torquehub.torquehub.domain.request.CommentDtos.CommentCreateRequest;
import torquehub.torquehub.domain.request.CommentDtos.CommentEditRequest;
import torquehub.torquehub.domain.response.CommentDtos.CommentResponse;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    JpaComment toEntity(CommentCreateRequest commentCreateRequest);
    JpaComment toEntity(CommentEditRequest commentEditRequest);

    @Mapping(target = "username", source = "jpaUser.username")
    @Mapping(target = "postedTime", source = "commentedTime")
    @Mapping(target = "userPoints", source = "jpaUser.points")
    CommentResponse toResponse(JpaComment jpaComment);
}
