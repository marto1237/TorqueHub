package torquehub.torquehub.business.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.interfaces.NotificationService;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.business.interfaces.VoteService;
import torquehub.torquehub.domain.ReputationConstants;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.model.jpa_models.JpaVote;
import torquehub.torquehub.domain.request.ReputationDtos.ReputationUpdateRequest;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;
import torquehub.torquehub.persistence.repository.VoteRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VoteServiceImpl implements VoteService {

    private final VoteRepository voteRepository;
    private final ReputationService reputationService;
    private final NotificationService notificationService;
    private final VoteServiceImpl self;

    public VoteServiceImpl(VoteRepository voteRepository,
                           ReputationService reputationService,
                           NotificationService notificationService,
                           VoteServiceImpl self) {
        this.voteRepository = voteRepository;
        this.reputationService = reputationService;
        this.notificationService = notificationService;
        this.self = self;
    }


    @Override
    @Transactional
    public ReputationResponse handleUpvote(JpaUser user, JpaQuestion question) {
        return self.handleVote(user, question, true);
    }

    @Override
    @Transactional
    public ReputationResponse handleDownvote(JpaUser user, JpaQuestion question) {
        return self.handleVote(user, question, false);
    }

    @Override
    @Transactional
    public ReputationResponse handleVote(JpaUser user, JpaQuestion question, boolean isUpvote) {
        Optional<JpaVote> existingVote = voteRepository.findByUserAndJpaQuestion(user, question);

        if (existingVote.isPresent()) {
            JpaVote vote = existingVote.get();
            if (vote.isUpvote() == isUpvote) {
                // Return the current reputation state without saving anything or sending notifications.
                return reputationService.getCurrentReputation(user.getId());
            }
            return self.processExistingVote(vote, question, user, isUpvote);
        } else {
            return self.processNewVote(user, question, isUpvote);
        }
    }

    @Override
    @Transactional
    public ReputationResponse processExistingVote(JpaVote vote, JpaQuestion question, JpaUser user, boolean isUpvote) {
        ReputationResponse response;

        // Handle changing the vote (from upvote to downvote or vice versa)
        boolean wasUpvoted = vote.isUpvote();  // Store previous vote type
        vote.setUpvote(isUpvote);              // Update the vote type to new value
        voteRepository.save(vote);             // Save the updated vote

        // Adjust vote count on the question
        question.setVotes(question.getVotes() + (isUpvote ? 2 : -2)); // +2 if switching to upvote, -2 if switching to downvote

        if (isUpvote) {
            response = reputationService.updateReputationForUpvote(new ReputationUpdateRequest(
                    question.getJpaUser().getId(), ReputationConstants.POINTS_UPVOTE_RECEIVED));
            reputationService.updateReputationForUpvoteGiven(new ReputationUpdateRequest(
                    user.getId(), ReputationConstants.POINTS_UPVOTE_GIVEN));

            // Notify only if the previous vote was not an upvote
            if (!wasUpvoted) {
                notificationService.notifyUserAboutVote(question.getJpaUser(),
                        "User " + user.getUsername() + " has upvoted your question: " + question.getTitle(),
                        user, question);
            }
        } else {
            response = reputationService.updateReputationForDownvote(new ReputationUpdateRequest(
                    question.getJpaUser().getId(), ReputationConstants.POINTS_DOWNVOTE_RECEIVED));
            reputationService.updateReputationForDownvoteGiven(new ReputationUpdateRequest(
                    user.getId(), ReputationConstants.POINTS_DOWNVOTE_GIVEN));
        }

        return response;
    }



    @Override
    @Transactional
    public ReputationResponse processNewVote(JpaUser user, JpaQuestion question, boolean isUpvote) {
        JpaVote newVote = JpaVote.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .upvote(isUpvote)
                .votedAt(LocalDateTime.now())
                .build();
        voteRepository.save(newVote);
        question.setVotes(question.getVotes() + (isUpvote ? 1 : -1));

        // Adjust reputation points
        ReputationResponse response;
        if (isUpvote) {
            response = reputationService.updateReputationForUpvote(new ReputationUpdateRequest(
                    question.getJpaUser().getId(), ReputationConstants.POINTS_UPVOTE_RECEIVED));
            reputationService.updateReputationForUpvoteGiven(new ReputationUpdateRequest(
                    user.getId(), ReputationConstants.POINTS_UPVOTE_GIVEN));

            // Notify user only through notificationService
            notificationService.notifyUserAboutVote(question.getJpaUser(),
                    "User " + user.getUsername() + " has upvoted your question: " + question.getTitle(),
                    user, question);
        } else {
            response = reputationService.updateReputationForDownvote(new ReputationUpdateRequest(
                    question.getJpaUser().getId(), ReputationConstants.POINTS_DOWNVOTE_RECEIVED));
            reputationService.updateReputationForDownvoteGiven(new ReputationUpdateRequest(
                    user.getId(), ReputationConstants.POINTS_DOWNVOTE_GIVEN));
        }
        return response;
    }



}