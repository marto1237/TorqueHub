package torquehub.torquehub.domain.mapper;


import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
        @Mapping(target = "userPoints", source = "user.points")
        @Mapping(target = "votes", source = "question.votes")
        @Mapping(target = "totalAnswers", expression = "java(question.getAnswers() != null ? question.getAnswers().size() : 0)")
        QuestionSummaryResponse toSummaryResponse(Question question);

        @Mapping(target = "answers", expression = "java(mapAnswersToPagedAnswerResponses(question.getAnswers(), pageable, commentMapper))")
        @Mapping(target = "tags", expression = "java(mapTagsToTagNames(question.getTags()))")
        @Mapping(target = "userName", source = "question.user.username")
        @Mapping(target = "userPoints", source = "question.user.points")
        QuestionDetailResponse toDetailResponse(Question question, @Context Pageable pageable, @Context CommentMapper commentMapper);

        default Set<String> mapTagsToTagNames(Set<Tag> tags) {
            if (tags == null) {
                return Collections.emptySet();
            }
            return tags.stream()
                    .map(Tag::getName)
                    .collect(Collectors.toSet());
        }

        default Set<Tag> mapTagNamesToTags(Set<String> tagNames) {
            return tagNames.stream()
                    .map(tagName -> {
                        Tag tag = new Tag();
                        tag.setName(tagName);
                        return tag;
                    })
                    .collect(Collectors.toSet());
        }

    // Helper method for mapping answers to Page<AnswerResponse>
    default Page<AnswerResponse> mapAnswersToPagedAnswerResponses(List<Answer> answers, Pageable pageable, CommentMapper commentMapper) {
        if (answers == null || answers.isEmpty()) {
            return Page.empty(pageable); // Handle empty pages correctly
        }

        // Convert List<Answer> to List<AnswerResponse>
        List<AnswerResponse> answerResponses = answers.stream()
                .map(answer -> {
                    AnswerResponse response = new AnswerResponse();
                    // You can add custom mappings for fields here
                    response.setId(answer.getId());
                    response.setText(answer.getText());
                    response.setUsername(answer.getUser().getUsername());
                    response.setVotes(answer.getVotes());
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

        // Convert List<AnswerResponse> to Page<AnswerResponse>
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), answerResponses.size());
        return new PageImpl<>(answerResponses.subList(start, end), pageable, answerResponses.size());
    }


}







