package torquehub.torquehub.domain.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.model.Tag;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.response.AnswerDtos.AnswerResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionDetailResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionSummaryResponse;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {AnswerMapper.class})
public interface QuestionMapper {

    Question toEntity(QuestionCreateRequest questionCreateRequest);

    Question toEntity(QuestionUpdateRequest questionUpdateRequest);

    @Mapping(target = "tags", expression = "java(mapTagsToTagNames(question.getTags()))")
    @Mapping(target = "username", source = "user.username")
    QuestionResponse toResponse(Question question);

    @Mapping(target = "tags", expression = "java(mapTagsToTagNames(question.getTags()))")
    @Mapping(target = "userName", source = "user.username")
    QuestionSummaryResponse toSummaryResponse(Question question);

    @Mapping(target = "answers", source = "answers")
    @Mapping(target = "tags", expression = "java(mapTagsToTagNames(question.getTags()))")
    @Mapping(target = "userName", source = "question.user.username")
    @Mapping(target = "userPoints", source = "question.user.points")
    QuestionDetailResponse toDetailResponse(Question question);

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
}




