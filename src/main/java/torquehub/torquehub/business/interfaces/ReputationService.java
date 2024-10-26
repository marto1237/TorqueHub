package torquehub.torquehub.business.interfaces;

import torquehub.torquehub.domain.request.reputation_dtos.ReputationUpdateRequest;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;

public interface ReputationService {
    ReputationResponse updateReputationForNewQuestion(ReputationUpdateRequest reputationUpdateRequest);

    boolean updateReputationForQuestionWhenQuestionIsDeleted(ReputationUpdateRequest reputationUpdateRequest);

    ReputationResponse updateReputationForNewAnswer(ReputationUpdateRequest reputationUpdateRequest);

    boolean updateReputationForAnswerWhenAnswerIsDeleted(ReputationUpdateRequest reputationUpdateRequest);

    ReputationResponse updateReputationForUpvote(ReputationUpdateRequest reputationUpdateRequest);

    ReputationResponse updateReputationForUpvoteRemoved(ReputationUpdateRequest reputationUpdateRequest);

    ReputationResponse updateReputationForUpvoteGivenRemoved(ReputationUpdateRequest reputationUpdateRequest);

    ReputationResponse updateReputationForDownvote(ReputationUpdateRequest reputationUpdateRequest);

    ReputationResponse updateReputationForUpvoteGiven(ReputationUpdateRequest reputationUpdateRequest);

    ReputationResponse updateReputationForDownvoteGiven(ReputationUpdateRequest reputationUpdateRequest);

    ReputationResponse updateReputationForBestAnswer(ReputationUpdateRequest reputationUpdateRequest);

    ReputationResponse updateReputationForBestAnswerIsDeleted(ReputationUpdateRequest reputationUpdateRequest);

    ReputationResponse updateReputationForConsecutiveActivity(ReputationUpdateRequest reputationUpdateRequest);

    ReputationResponse updateReputationForNewComment(ReputationUpdateRequest reputationUpdateRequest);

    boolean updateReputationForCommentWhenCommentIsDeleted(ReputationUpdateRequest reputationUpdateRequest);

    ReputationResponse updateReputationForUpvoteComment(ReputationUpdateRequest reputationUpdateRequest);

    ReputationResponse updateReputationForDownvoteComment(ReputationUpdateRequest reputationUpdateRequest);

    ReputationResponse getCurrentReputation(Long userId);
}
