package torquehub.torquehub.business.interfaces;

import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.model.jpa_models.JpaVote;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;

public interface VoteService {
    ReputationResponse handleUpvote(JpaUser user, JpaQuestion question);
    ReputationResponse handleDownvote(JpaUser user, JpaQuestion question);
    ReputationResponse handleVote(JpaUser user, JpaQuestion question, boolean isUpvote);
    ReputationResponse processExistingVote(JpaVote vote, JpaQuestion question, JpaUser user, boolean isUpvote);
    ReputationResponse processNewVote(JpaUser user, JpaQuestion question, boolean isUpvote);
}
