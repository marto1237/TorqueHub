package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import torquehub.torquehub.domain.model.jpa_models.JpaFollow;
import torquehub.torquehub.domain.request.FollowDtos.FollowAnswerRequest;
import torquehub.torquehub.domain.request.FollowDtos.FollowQuestionRequest;
import torquehub.torquehub.domain.response.FollowRequest.FollowResponse;

@Mapper(componentModel = "spring")
public interface FollowMapper {

    JpaFollow toEntity(FollowQuestionRequest followQuestionRequest);
    JpaFollow toEntity(FollowAnswerRequest followAnswerRequest);
    FollowResponse toResponse(JpaFollow jpaFollow);
}
