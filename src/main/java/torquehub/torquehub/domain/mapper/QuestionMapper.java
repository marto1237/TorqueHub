package torquehub.torquehub.domain.mapper;


import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.model.Answer;
import torquehub.torquehub.domain.model.Comment;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.model.Tag;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.response.AnswerDtos.AnswerResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionDetailResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionSummaryResponse;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {AnswerMapper.class, CommentMapper.class})
public interface QuestionMapper {

    Question toEntity(QuestionCreateRequest questionCreateRequest);

    Question toEntity(QuestionUpdateRequest questionUpdateRequest);

    @Mapping(target = "tags", expression = "java(mapTagsToTagNames(question.getTags()))")
    @Mapping(target = "username", source = "user.username")
    QuestionResponse toResponse(Question question);

    @Mapping(target = "tags", expression = "java(mapTagsToTagNames(question.getTags()))")
    @Mapping(target = "userName", source = "user.username")
    QuestionSummaryResponse toSummaryResponse(Question question);

    // Pass both answers and commentMapper in the expression
    @Mapping(target = "answers", expression = "java(mapAnswersToAnswerResponses(question.getAnswers(), commentMapper))")
    @Mapping(target = "tags", expression = "java(mapTagsToTagNames(question.getTags()))")
    @Mapping(target = "userName", source = "question.user.username")
    @Mapping(target = "userPoints", source = "question.user.points")
    QuestionDetailResponse toDetailResponse(Question question, @Context CommentMapper commentMapper);

    // Default method to map Tags to Strings
    default Set<String> mapTagsToTagNames(Set<Tag> tags) {
        if (tags == null) {
            return Collections.emptySet();
        }
        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
    }

    // Default method to map Strings to Tags
    default Set<Tag> mapTagNamesToTags(Set<String> tagNames) {
        return tagNames.stream()
                .map(tagName -> {
                    Tag tag = new Tag();
                    tag.setName(tagName);
                    return tag;
                })
                .collect(Collectors.toSet());
    }

    // Default method to map Answers to AnswerResponses
    default List<AnswerResponse> mapAnswersToAnswerResponses(List<Answer> answers, @Context CommentMapper commentMapper) {
        if (answers == null || answers.isEmpty()) {
            return Collections.emptyList();
        }
        return answers.stream()
                .map(answer -> {
                    AnswerResponse response = new AnswerResponse();
                    response.setId(answer.getId());
                    response.setText(answer.getText());
                    response.setUsername(answer.getUser().getUsername());
                    response.setVotes(answer.getVotes());
                    response.setEdited(answer.isEdited());
                    response.setPostedTime(java.util.Date.from(answer.getAnsweredTime().atZone(java.time.ZoneId.systemDefault()).toInstant()));

                    // Map comments using CommentMapper
                    response.setComments(
                            answer.getComments().stream()
                                    .map(commentMapper::toResponse)
                                    .collect(Collectors.toList())
                    );
                    return response;
                })
                .collect(Collectors.toList());
    }

}






