package torquehub.torquehub.business.interfaces;

import org.springframework.data.domain.Page;
import torquehub.torquehub.domain.request.follow_dtos.FollowAnswerRequest;
import torquehub.torquehub.domain.request.follow_dtos.FollowQuestionRequest;
import torquehub.torquehub.domain.request.follow_dtos.FollowedAnswerRequest;
import torquehub.torquehub.domain.request.follow_dtos.FollowedQuestionRequest;
import torquehub.torquehub.domain.response.follow_dtos.FollowResponse;

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

}
