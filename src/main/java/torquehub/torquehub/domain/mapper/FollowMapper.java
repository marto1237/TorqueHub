package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import torquehub.torquehub.domain.model.jpa_models.JpaFollow;
import torquehub.torquehub.domain.request.follow_dtos.FollowAnswerRequest;
import torquehub.torquehub.domain.request.follow_dtos.FollowQuestionRequest;
import torquehub.torquehub.domain.response.follow_dtos.FollowResponse;

@Mapper(componentModel = "spring")
public interface FollowMapper {

    JpaFollow toEntity(FollowQuestionRequest followQuestionRequest);
    JpaFollow toEntity(FollowAnswerRequest followAnswerRequest);
    FollowResponse toResponse(JpaFollow jpaFollow);
}
