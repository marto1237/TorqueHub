package torquehub.torquehub.domain.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.model.Answer;
import torquehub.torquehub.domain.model.Comment;
import torquehub.torquehub.domain.request.AnswerDtos.AnswerCreateRequest;
import torquehub.torquehub.domain.request.AnswerDtos.AnswerEditRequest;
import torquehub.torquehub.domain.response.AnswerDtos.AnswerResponse;
import torquehub.torquehub.domain.response.CommentDtos.CommentResponse;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {CommentMapper.class})
public interface AnswerMapper {

    Answer toEntity(AnswerCreateRequest answerCreateRequest);
    Answer toEntity(AnswerEditRequest answerEditRequest);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "userPoints", source = "user.points")
    @Mapping(target = "isEdited", source = "edited")
    @Mapping(target = "comments", expression = "java(limitComments(answer.getComments(), 0, commentMapper))")
    @Mapping(target = "postedTime", expression = "java(java.util.Date.from(answer.getAnsweredTime().atZone(java.time.ZoneId.systemDefault()).toInstant()))")
    AnswerResponse toResponse(Answer answer, @Context CommentMapper commentMapper);

    default List<CommentResponse> limitComments(List<Comment> comments, int startIndex, @Context CommentMapper commentMapper) {
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }

        // Limit to the top 5 comments, starting at startIndex
        return comments.stream()
                .skip(startIndex) // Skip comments if we want to paginate
                .limit(5)         // Limit to 5 comments
                .map(commentMapper::toResponse)  // Map Comment to CommentResponse
                .collect(Collectors.toList());
    }

}
