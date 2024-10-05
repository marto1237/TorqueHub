package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import torquehub.torquehub.domain.model.Follow;
import torquehub.torquehub.domain.request.FollowDtos.FollowAnswerRequest;
import torquehub.torquehub.domain.request.FollowDtos.FollowQuestionRequest;
import torquehub.torquehub.domain.response.FollowRequest.FollowResponse;

@Mapper(componentModel = "spring")
public interface FollowMapper {

    Follow toEntity(FollowQuestionRequest followQuestionRequest);
    Follow toEntity(FollowAnswerRequest followAnswerRequest);
    FollowResponse toResponse(Follow follow);
}
