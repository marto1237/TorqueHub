package torquehub.torquehub.business.interfaces;

import torquehub.torquehub.domain.request.follow_dtos.FollowAnswerRequest;
import torquehub.torquehub.domain.request.follow_dtos.FollowQuestionRequest;
import torquehub.torquehub.domain.response.follow_dtos.FollowResponse;

import java.util.List;

public interface FollowService {
    FollowResponse toggleFollowQuestion(FollowQuestionRequest followQuestionRequest);
    FollowResponse toggleFollowAnswer(FollowAnswerRequest followAnswerRequest);
    boolean muteNotifications(Long followId);
    List<FollowResponse> getUserFollows(Long userId);

}
