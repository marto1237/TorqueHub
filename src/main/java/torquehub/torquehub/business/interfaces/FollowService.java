package torquehub.torquehub.business.interfaces;

import org.springframework.data.domain.Page;
import torquehub.torquehub.domain.request.follow_dtos.*;
import torquehub.torquehub.domain.response.follow_dtos.FollowResponse;
import torquehub.torquehub.domain.response.follow_dtos.FollowedAnswerResponse;
import torquehub.torquehub.domain.response.follow_dtos.FollowedQuestionResponse;

import java.util.List;

public interface FollowService {
    FollowResponse toggleFollowQuestion(FollowQuestionRequest followQuestionRequest);
    FollowResponse toggleFollowAnswer(FollowAnswerRequest followAnswerRequest);
    boolean muteNotifications(Long followId);
    List<FollowResponse> getUserFollows(Long userId);
    Page<FollowResponse> getFollowedQuestions(FollowedQuestionRequest followedQuestionRequest);
    Page<FollowResponse> getFollowedAnswers(FollowedAnswerRequest followedAnswerRequest);
    boolean batchMuteFollows(List<Long> followIds);
    boolean batchUnfollow(List<Long> followIds);
    boolean muteFollow(MuteFollowRequest muteFollowRequest);
    boolean batchToggleMuteFollows(List<MuteFollowRequest> muteRequests);
    Page<FollowedQuestionResponse> getQuestions(FollowedQuestionRequest request);
    Page<FollowedAnswerResponse> getAnswers(FollowedAnswerRequest request);


}
