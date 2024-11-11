package torquehub.torquehub.domain.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;
import torquehub.torquehub.domain.request.question_dtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.question_dtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionDetailResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {AnswerMapper.class, CommentMapper.class})
public interface QuestionMapper {

    JpaQuestion toEntity(QuestionCreateRequest questionCreateRequest);

    JpaQuestion toEntity(QuestionUpdateRequest questionUpdateRequest);

    @Mapping(target = "tags", expression = "java(mapTagsToTagNames(jpaQuestion.getJpaTags()))")
    @Mapping(target = "username", source = "jpaQuestion.jpaUser.username")
    QuestionResponse toResponse(JpaQuestion jpaQuestion);

    @Mapping(target = "tags", expression = "java(mapTagsToTagNames(jpaQuestion.getJpaTags()))")
    @Mapping(target = "userName", source = "jpaQuestion.jpaUser.username")
    @Mapping(target = "userPoints", source = "jpaQuestion.jpaUser.points")
    @Mapping(target = "votes", source = "jpaQuestion.votes")
    @Mapping(target = "totalAnswers", expression = "java(jpaQuestion.getJpaAnswers() != null ? jpaQuestion.getJpaAnswers().size() : 0)")
    QuestionSummaryResponse toSummaryResponse(JpaQuestion jpaQuestion);

    @Mapping(target = "answers", expression = "java(mapAnswersToPagedAnswerResponses(jpaQuestion.getJpaAnswers(), context))")
    @Mapping(target = "tags", expression = "java(mapTagsToTagNames(jpaQuestion.getJpaTags()))")
    @Mapping(target = "userName", source = "jpaQuestion.jpaUser.username")
    @Mapping(target = "userPoints", source = "jpaQuestion.jpaUser.points")
    QuestionDetailResponse toDetailResponse(
            JpaQuestion jpaQuestion,
            @Context QuestionMapperContext context);

    default Set<String> mapTagsToTagNames(Set<JpaTag> jpaTags) {
        if (jpaTags == null) {
            return Collections.emptySet();
        }
        return jpaTags.stream()
                .map(JpaTag::getName)
                .collect(Collectors.toSet());
    }

    default Set<JpaTag> mapTagNamesToTags(Set<String> tagNames) {
        return tagNames.stream()
                .map(tagName -> {
                    JpaTag jpaTag = new JpaTag();
                    jpaTag.setName(tagName);
                    return jpaTag;
                })
                .collect(Collectors.toSet());
    }

    // Helper method for mapping answers to Page<AnswerResponse>
    default Page<AnswerResponse> mapAnswersToPagedAnswerResponses(List<JpaAnswer> jpaAnswers, QuestionMapperContext context) {
        if (jpaAnswers == null || jpaAnswers.isEmpty()) {
            return Page.empty(context.getPageable()); // Handle empty pages correctly
        }

        // Convert List<Answer> to List<AnswerResponse>
        List<AnswerResponse> answerResponses = jpaAnswers.stream()
                .map(answer -> context.getAnswerMapper().toResponse(answer, context.getUserId(),
                        context.getBookmarkRepository(), context.getFollowRepository(),
                        context.getVoteRepository(), context.getCommentMapper()))
                .toList();

        // Convert List<AnswerResponse> to Page<AnswerResponse>
        int start = (int) context.getPageable().getOffset();
        int end = Math.min((start + context.getPageable().getPageSize()), answerResponses.size());
        return new PageImpl<>(answerResponses.subList(start, end), context.getPageable(), answerResponses.size());
    }
}
