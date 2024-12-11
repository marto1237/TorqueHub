package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.model.jpa_models.JpaFollow;
import torquehub.torquehub.domain.request.follow_dtos.FollowAnswerRequest;
import torquehub.torquehub.domain.request.follow_dtos.FollowQuestionRequest;
import torquehub.torquehub.domain.response.follow_dtos.FollowResponse;
import torquehub.torquehub.domain.response.follow_dtos.FollowedAnswerResponse;
import torquehub.torquehub.domain.response.follow_dtos.FollowedQuestionResponse;

@Mapper(componentModel = "spring")
public interface FollowMapper {

    JpaFollow toEntity(FollowQuestionRequest followQuestionRequest);
    JpaFollow toEntity(FollowAnswerRequest followAnswerRequest);
    FollowResponse toResponse(JpaFollow jpaFollow);

    @Mapping(target = "followId", source = "id")
    @Mapping(target = "questionId", source = "jpaQuestion.id")
    @Mapping(target = "title", source = "jpaQuestion.title")
    @Mapping(target = "description", source = "jpaQuestion.description")
    @Mapping(target = "tags", expression = "java(jpaFollow.getJpaQuestion().getJpaTags().stream().map(tag -> tag.getName()).collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "views", source = "jpaQuestion.views")
    @Mapping(target = "votes", source = "jpaQuestion.votes")
    @Mapping(target = "totalAnswers", source = "jpaQuestion.totalAnswers")
    @Mapping(target = "username", source = "jpaQuestion.jpaUser.username")
    @Mapping(target = "isMuted", source = "muted")
    @Mapping(target = "askedTime", source = "jpaQuestion.askedTime")
    FollowedQuestionResponse toFollowedQuestionResponse(JpaFollow jpaFollow);

    @Mapping(target = "followId", source = "id")
    @Mapping(target = "answerId", source = "jpaAnswer.id")
    @Mapping(target = "text", source = "jpaAnswer.text")
    @Mapping(target = "username", source = "jpaAnswer.jpaUser.username")
    @Mapping(target = "votes", source = "jpaAnswer.votes")
    @Mapping(target = "isMuted", source = "muted")
    @Mapping(target = "isEdited", source = "jpaAnswer.edited")
    @Mapping(target = "postedTime", source = "jpaAnswer.answeredTime")
    FollowedAnswerResponse toFollowedAnswerResponse(JpaFollow jpaFollow);
}
