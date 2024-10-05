package torquehub.torquehub.business.interfaces;

import torquehub.torquehub.domain.request.FollowDtos.FollowAnswerRequest;
import torquehub.torquehub.domain.request.FollowDtos.FollowQuestionRequest;
import torquehub.torquehub.domain.response.FollowRequest.FollowResponse;

import java.util.List;

public interface FollowService {
    FollowResponse followQuestion(FollowQuestionRequest followQuestionRequest);
    FollowResponse followResponse(FollowAnswerRequest followAnswerRequest);
    boolean muteNotifications(Long followId);
    List<FollowResponse> getUserFollows(Long userId);

}
