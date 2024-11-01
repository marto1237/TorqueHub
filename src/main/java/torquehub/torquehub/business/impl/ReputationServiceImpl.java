package torquehub.torquehub.business.impl;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.domain.ReputationConstants;
import torquehub.torquehub.domain.mapper.ReputationMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.request.reputation_dtos.ReputationUpdateRequest;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaUserRepository;

import java.util.Optional;

@Service
public class ReputationServiceImpl  implements ReputationService {

    private final JpaUserRepository userRepository;
    private final ReputationMapper reputationMapper;

    public ReputationServiceImpl(JpaUserRepository userRepository, ReputationMapper reputationMapper) {
        this.userRepository = userRepository;
        this.reputationMapper = reputationMapper;
    }

    @Override
    @Transactional
    public ReputationResponse updateReputationForNewQuestion(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_NEW_QUESTION, "New Question Posted");
    }

    @Override
    @Transactional
    public boolean updateReputationForQuestionWhenQuestionIsDeleted(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_QUESTION_WHEN_DELETED, "Question Deleted") != null;
    }

    @Override
    @Transactional
    public ReputationResponse updateReputationForNewAnswer(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_NEW_ANSWER, "New Answer Posted");
    }

    @Override
    public boolean updateReputationForAnswerWhenAnswerIsDeleted(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_ANSWER_WHEN_DELETED, "Answer Deleted") != null;
    }

    @Override
    @Transactional
    public ReputationResponse updateReputationForUpvote(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_UPVOTE_RECEIVED, "Upvote Received");
    }

    @Override
    public ReputationResponse updateReputationForUpvoteRemoved(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_DOWNVOTE_RECEIVED, "Upvote Removed");
    }

    @Override
    public ReputationResponse updateReputationForUpvoteGivenRemoved(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_DOWNVOTE_GIVEN, "Upvote Given Removed");
    }

    @Override
    @Transactional
    public ReputationResponse updateReputationForDownvote(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_DOWNVOTE_RECEIVED, "Downvote Received");
    }

    @Override
    @Transactional
    public ReputationResponse updateReputationForUpvoteGiven(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_UPVOTE_GIVEN, "Upvote Given");
    }

    @Override
    @Transactional
    public ReputationResponse updateReputationForDownvoteGiven(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_DOWNVOTE_GIVEN, "Downvote Given");
    }

    @Override
    @Transactional
    public ReputationResponse updateReputationForBestAnswer(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_BEST_ANSWER, "Best Answer Awarded");
    }

    @Override
    public ReputationResponse updateReputationForBestAnswerIsDeleted(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_BEST_ANSWER_WHEN_DELETED, "Best Answer Removed");
    }

    @Override
    @Transactional
    public ReputationResponse updateReputationForConsecutiveActivity(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_CONSECUTIVE_ACTIVITY, "Consecutive Activity Points");
    }

    @Override
    @Transactional
    public ReputationResponse updateReputationForNewComment(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_NEW_COMMENT, "New Comment Posted");
    }

    @Override
    public boolean updateReputationForCommentWhenCommentIsDeleted(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_COMMENT_WHEN_DELETED, "Comment Deleted") != null;
    }

    @Override
    @Transactional
    public ReputationResponse updateReputationForUpvoteComment(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_UPVOTE_COMMENT, "Comment Upvoted");
    }

    @Override
    @Transactional
    public ReputationResponse updateReputationForDownvoteComment(ReputationUpdateRequest reputationUpdateRequest) {
        return updateReputation(reputationUpdateRequest.getUserId(), ReputationConstants.POINTS_DOWNVOTE_COMMENT, "Comment Downvoted");
    }

    @Override
    @Cacheable(value = "currentReputation", key = "#userId")
    public ReputationResponse getCurrentReputation(Long userId) {
        try {
            Optional<JpaUser> userOptional = userRepository.findById(userId);
            if(userOptional.isPresent()) {
                JpaUser jpaUser = userOptional.get();
                return reputationMapper.toResponse(jpaUser, "Current Reputation", jpaUser.getPoints());
            }else {
                throw new IllegalArgumentException("User with ID " + userId + " not found.");
            }
        }catch (Exception e) {
            throw new IllegalArgumentException("Error getting reputation for user with ID " + userId + ": " + e.getMessage());
        }
    }

    private ReputationResponse updateReputation(Long userId, int points, String action) {
        try {
            Optional<JpaUser> userOptional = userRepository.findById(userId);
            if(userOptional.isPresent()) {
                JpaUser jpaUser = userOptional.get();

                int newPoints = jpaUser.getPoints() + points;

                jpaUser.setPoints(newPoints);
                userRepository.save(jpaUser);

                return reputationMapper.toResponse(jpaUser, action, points);
            }else {
                throw new IllegalArgumentException("User with ID " + userId + " not found.");
            }
        }catch (Exception e) {
            throw new IllegalArgumentException("Error updating reputation for user with ID " + userId + ": " + e.getMessage());
        }
    }


}
