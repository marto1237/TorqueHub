package torquehub.torquehub.business.interfaces;

import torquehub.torquehub.domain.model.jpa_models.*;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;

public interface VoteService {
    ReputationResponse handleUpvote(JpaUser user, JpaQuestion question);
    ReputationResponse handleDownvote(JpaUser user, JpaQuestion question);
    ReputationResponse handleVote(JpaUser user, JpaQuestion question, boolean isUpvote);
    ReputationResponse processExistingVote(JpaVote vote, JpaQuestion question, JpaUser user, boolean isUpvote);
    ReputationResponse processNewVote(JpaUser user, JpaQuestion question, boolean isUpvote);

    ReputationResponse handleUpvoteForAnswer(JpaUser user, JpaAnswer answer);
    ReputationResponse handleDownvoteForAnswer(JpaUser user, JpaAnswer answer);
    ReputationResponse handleVoteForAnswer(JpaUser user, JpaAnswer answer, boolean isUpvote);
    ReputationResponse processExistingVoteForAnswer(JpaVote vote, JpaAnswer answer, JpaUser user, boolean isUpvote);
    ReputationResponse processNewVoteForAnswer(JpaUser user, JpaAnswer answer, boolean isUpvote);

    ReputationResponse handleUpvoteForComment(JpaUser user, JpaComment comment);
    ReputationResponse handleDownvoteForComment(JpaUser user, JpaComment comment);
    ReputationResponse handleVoteForComment(JpaUser user, JpaComment comment, boolean isUpvote);
    ReputationResponse processExistingVoteForComment(JpaVote vote, JpaComment comment, JpaUser user, boolean isUpvote);
    ReputationResponse processNewVoteForComment(JpaUser user, JpaComment comment, boolean isUpvote);


}
