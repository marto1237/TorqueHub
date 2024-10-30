package torquehub.torquehub.business.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.interfaces.NotificationService;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.business.interfaces.VoteService;
import torquehub.torquehub.domain.ReputationConstants;
import torquehub.torquehub.domain.model.jpa_models.*;
import torquehub.torquehub.domain.request.reputation_dtos.ReputationUpdateRequest;
import torquehub.torquehub.domain.request.vote_dtos.VoteAnswerNotificationRequest;
import torquehub.torquehub.domain.request.vote_dtos.VoteCommentNotificationRequest;
import torquehub.torquehub.domain.request.vote_dtos.VoteQuestionNotificationRequest;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;
import torquehub.torquehub.persistence.repository.VoteRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VoteServiceImpl implements VoteService {

    private final VoteRepository voteRepository;
    private final ReputationService reputationService;
    private final NotificationService notificationService;

    public VoteServiceImpl(VoteRepository voteRepository,
                           ReputationService reputationService,
                           NotificationService notificationService) {
        this.voteRepository = voteRepository;
        this.reputationService = reputationService;
        this.notificationService = notificationService;
    }

    private static final String USER_PREFIX = "User ";

    @Override
    @Transactional
    public ReputationResponse handleUpvote(JpaUser user, JpaQuestion question) {
        return handleVote(user, question, true);
    }

    @Override
    @Transactional
    public ReputationResponse handleDownvote(JpaUser user, JpaQuestion question) {
        return handleVote(user, question, false);
    }

    public ReputationResponse handleVote(JpaUser user, JpaQuestion question, boolean isUpvote) {
        Optional<JpaVote> existingVote = voteRepository.findByUserAndJpaQuestion(user, question);

        if (existingVote.isPresent()) {
            JpaVote vote = existingVote.get();
            if (vote.isUpvote() == isUpvote) {
                // Return the current reputation state without saving anything or sending notifications.
                return reputationService.getCurrentReputation(user.getId());
            }
            return processExistingVote(vote, question, user, isUpvote);
        } else {
            return processNewVote(user, question, isUpvote);
        }
    }

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
                VoteQuestionNotificationRequest request = new VoteQuestionNotificationRequest(
                        question.getJpaUser().getId(),
                        USER_PREFIX + user.getUsername() + " has upvoted your question: " + question.getTitle(),
                        user.getId(),
                        question.getId()
                );

                notificationService.notifyUserAboutQuestionVote(request);
            }
        } else {
            response = reputationService.updateReputationForDownvote(new ReputationUpdateRequest(
                    question.getJpaUser().getId(), ReputationConstants.POINTS_DOWNVOTE_RECEIVED));
            reputationService.updateReputationForDownvoteGiven(new ReputationUpdateRequest(
                    user.getId(), ReputationConstants.POINTS_DOWNVOTE_GIVEN));
        }

        return response;
    }


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

            VoteQuestionNotificationRequest request = new VoteQuestionNotificationRequest(
                    question.getJpaUser().getId(),
                    USER_PREFIX + user.getUsername() + " has upvoted your question: " + question.getTitle(),
                    user.getId(),
                    question.getId()
            );
            notificationService.notifyUserAboutQuestionVote(request);
        } else {
            response = reputationService.updateReputationForDownvote(new ReputationUpdateRequest(
                    question.getJpaUser().getId(), ReputationConstants.POINTS_DOWNVOTE_RECEIVED));
            reputationService.updateReputationForDownvoteGiven(new ReputationUpdateRequest(
                    user.getId(), ReputationConstants.POINTS_DOWNVOTE_GIVEN));
        }
        return response;
    }

    @Override
    public ReputationResponse handleUpvoteForAnswer(JpaUser user, JpaAnswer answer) {
        return handleVoteForAnswer(user, answer, true);
    }

    @Override
    public ReputationResponse handleDownvoteForAnswer(JpaUser user, JpaAnswer answer) {
        return handleVoteForAnswer(user, answer, false);
    }

    @Override
    public ReputationResponse handleVoteForAnswer(JpaUser user, JpaAnswer answer, boolean isUpvote) {
        Optional<JpaVote> existingVote = voteRepository.findByUserAndJpaAnswer(user, answer);

        if (existingVote.isPresent()) {
            JpaVote vote = existingVote.get();
            if (vote.isUpvote() == isUpvote) {
                // Return the current reputation state without saving anything or sending notifications.
                return reputationService.getCurrentReputation(user.getId());
            }
            return processExistingVoteForAnswer(vote, answer, user, isUpvote);
        } else {
            return processNewVoteForAnswer(user, answer, isUpvote);
        }
    }

    @Override
    public ReputationResponse processExistingVoteForAnswer(JpaVote vote, JpaAnswer answer, JpaUser user, boolean isUpvote) {
        ReputationResponse response;

        // Handle changing the vote (from upvote to downvote or vice versa)
        boolean wasUpvoted = vote.isUpvote();  // Store previous vote type
        vote.setUpvote(isUpvote);              // Update the vote type to new value
        voteRepository.save(vote);             // Save the updated vote

        // Adjust vote count on the answer
        answer.setVotes(answer.getVotes() + (isUpvote ? 2 : -2)); // +2 if switching to upvote, -2 if switching to downvote

        if (isUpvote) {
            response = reputationService.updateReputationForUpvote(new ReputationUpdateRequest(
                    answer.getJpaUser().getId(), ReputationConstants.POINTS_UPVOTE_RECEIVED));
            reputationService.updateReputationForUpvoteGiven(new ReputationUpdateRequest(
                    user.getId(), ReputationConstants.POINTS_UPVOTE_GIVEN));

            // Notify only if the previous vote was not an upvote
            if (!wasUpvoted) {
                VoteAnswerNotificationRequest request = new VoteAnswerNotificationRequest(
                        answer.getJpaUser().getId(),
                        USER_PREFIX + user.getUsername() + " has upvoted your answer",
                        user.getId(),
                        answer.getId()
                );

                notificationService.notifyUserAboutAnswerVote(request);
            }
        } else {
            response = reputationService.updateReputationForDownvote(new ReputationUpdateRequest(
                    answer.getJpaUser().getId(), ReputationConstants.POINTS_DOWNVOTE_RECEIVED));
            reputationService.updateReputationForDownvoteGiven(new ReputationUpdateRequest(
                    user.getId(), ReputationConstants.POINTS_DOWNVOTE_GIVEN));
        }

        return response;
    }

    @Override
    public ReputationResponse processNewVoteForAnswer(JpaUser user, JpaAnswer answer, boolean isUpvote) {
        JpaVote newVote = JpaVote.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .upvote(isUpvote)
                .votedAt(LocalDateTime.now())
                .build();
        voteRepository.save(newVote);
        answer.setVotes(answer.getVotes() + (isUpvote ? 1 : -1));

        // Adjust reputation points
        ReputationResponse response;
        if (isUpvote) {
            response = reputationService.updateReputationForUpvote(new ReputationUpdateRequest(
                    answer.getJpaUser().getId(), ReputationConstants.POINTS_UPVOTE_RECEIVED));
            reputationService.updateReputationForUpvoteGiven(new ReputationUpdateRequest(
                    user.getId(), ReputationConstants.POINTS_UPVOTE_GIVEN));

            // Notify user only through notificationService
            VoteAnswerNotificationRequest request = new VoteAnswerNotificationRequest(
                    answer.getJpaUser().getId(),
                    USER_PREFIX + user.getUsername() + " has upvoted your answer",
                    user.getId(),
                    answer.getId()
            );
            notificationService.notifyUserAboutAnswerVote(request);
        } else {
            response = reputationService.updateReputationForDownvote(new ReputationUpdateRequest(
                    answer.getJpaUser().getId(), ReputationConstants.POINTS_DOWNVOTE_RECEIVED));
            reputationService.updateReputationForDownvoteGiven(new ReputationUpdateRequest(
                    user.getId(), ReputationConstants.POINTS_DOWNVOTE_GIVEN));
        }
        return response;
    }

    @Override
    @Transactional
    public ReputationResponse handleUpvoteForComment(JpaUser user, JpaComment comment) {
        return handleVoteForComment(user, comment, true);
    }

    @Override
    @Transactional
    public ReputationResponse handleDownvoteForComment(JpaUser user, JpaComment comment) {
        return handleVoteForComment(user, comment, false);
    }

    public ReputationResponse handleVoteForComment(JpaUser user, JpaComment comment, boolean isUpvote) {
        Optional<JpaVote> existingVote = voteRepository.findByUserAndJpaComment(user, comment);

        if (existingVote.isPresent()) {
            JpaVote vote = existingVote.get();
            if (vote.isUpvote() == isUpvote) {
                // Return the current reputation state without saving anything or sending notifications.
                return reputationService.getCurrentReputation(user.getId());
            }
            return processExistingVoteForComment(vote, comment, user, isUpvote);
        } else {
            return processNewVoteForComment(user, comment, isUpvote);
        }
    }

    public ReputationResponse processExistingVoteForComment(JpaVote vote, JpaComment comment, JpaUser user, boolean isUpvote) {
        ReputationResponse response;

        // Handle changing the vote (from upvote to downvote or vice versa)
        boolean wasUpvoted = vote.isUpvote();
        vote.setUpvote(isUpvote);
        voteRepository.save(vote);

        // Adjust vote count on the comment
        comment.setVotes(comment.getVotes() + (isUpvote ? 2 : -2)); // +2 if switching to upvote, -2 if switching to downvote

        if (isUpvote) {
            response = reputationService.updateReputationForUpvote(new ReputationUpdateRequest(
                    comment.getJpaUser().getId(), ReputationConstants.POINTS_UPVOTE_RECEIVED));
            reputationService.updateReputationForUpvoteGiven(new ReputationUpdateRequest(
                    user.getId(), ReputationConstants.POINTS_UPVOTE_GIVEN));

            // Notify only if the previous vote was not an upvote
            if (!wasUpvoted) {
                VoteCommentNotificationRequest request = new VoteCommentNotificationRequest(
                        comment.getJpaUser().getId(),
                        USER_PREFIX + user.getUsername() + " has upvoted your comment",
                        user.getId(),
                        comment.getId()
                );
                notificationService.notifyUserAboutCommentVote(request);
            }
        } else {
            response = reputationService.updateReputationForDownvote(new ReputationUpdateRequest(
                    comment.getJpaUser().getId(), ReputationConstants.POINTS_DOWNVOTE_RECEIVED));
            reputationService.updateReputationForDownvoteGiven(new ReputationUpdateRequest(
                    user.getId(), ReputationConstants.POINTS_DOWNVOTE_GIVEN));
        }

        return response;
    }

    public ReputationResponse processNewVoteForComment(JpaUser user, JpaComment comment, boolean isUpvote) {
        JpaVote newVote = JpaVote.builder()
                .jpaUser(user)
                .jpaComment(comment)
                .upvote(isUpvote)
                .votedAt(LocalDateTime.now())
                .build();
        voteRepository.save(newVote);
        comment.setVotes(comment.getVotes() + (isUpvote ? 1 : -1));

        // Adjust reputation points
        ReputationResponse response;
        if (isUpvote) {
            response = reputationService.updateReputationForUpvote(new ReputationUpdateRequest(
                    comment.getJpaUser().getId(), ReputationConstants.POINTS_UPVOTE_RECEIVED));
            reputationService.updateReputationForUpvoteGiven(new ReputationUpdateRequest(
                    user.getId(), ReputationConstants.POINTS_UPVOTE_GIVEN));

            // Notify user only through notificationService

            VoteCommentNotificationRequest request = new VoteCommentNotificationRequest(
                    comment.getJpaUser().getId(),
                    USER_PREFIX + user.getUsername() + " has upvoted your comment: " + comment.getText(),
                    user.getId(),
                    comment.getId()
            );
            notificationService.notifyUserAboutCommentVote(request);
        } else {
            response = reputationService.updateReputationForDownvote(new ReputationUpdateRequest(
                    comment.getJpaUser().getId(), ReputationConstants.POINTS_DOWNVOTE_RECEIVED));
            reputationService.updateReputationForDownvoteGiven(new ReputationUpdateRequest(
                    user.getId(), ReputationConstants.POINTS_DOWNVOTE_GIVEN));
        }
        return response;
    }

}