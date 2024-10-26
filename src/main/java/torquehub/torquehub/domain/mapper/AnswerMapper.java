package torquehub.torquehub.domain.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.model.jpa_models.JpaComment;
import torquehub.torquehub.domain.request.answer_dtos.AnswerCreateRequest;
import torquehub.torquehub.domain.request.answer_dtos.AnswerEditRequest;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.comment_dtos.CommentResponse;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CommentMapper.class})
public interface AnswerMapper {

    JpaAnswer toEntity(AnswerCreateRequest answerCreateRequest);
    JpaAnswer toEntity(AnswerEditRequest answerEditRequest);

    @Mapping(target = "username", source = "jpaUser.username")
    @Mapping(target = "userPoints", source = "jpaUser.points")
    @Mapping(target = "isEdited", source = "edited")
    @Mapping(target = "comments", expression = "java(limitComments(jpaAnswer.getJpaComments(), 0, commentMapper))")
    @Mapping(target = "postedTime", expression = "java(java.util.Date.from(jpaAnswer.getAnsweredTime().atZone(java.time.ZoneId.systemDefault()).toInstant()))")
    AnswerResponse toResponse(JpaAnswer jpaAnswer, @Context CommentMapper commentMapper);

    default List<CommentResponse> limitComments(List<JpaComment> jpaComments, int startIndex, @Context CommentMapper commentMapper) {
        if (jpaComments == null || jpaComments.isEmpty()) {
            return List.of();
        }

        // Limit to the top 5 comments, starting at startIndex
        return jpaComments.stream()
                .skip(startIndex) // Skip comments if we want to paginate
                .limit(5)         // Limit to 5 comments
                .map(commentMapper::toResponse)  // Map Comment to CommentResponse
                .toList();
    }

}
