package torquehub.torquehub.business.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import torquehub.torquehub.domain.ReputationConstants;
import torquehub.torquehub.domain.mapper.ReputationMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.request.reputation_dtos.ReputationUpdateRequest;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaUserRepository;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReputationServiceImplTest {

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private ReputationMapper reputationMapper;

    @InjectMocks
    private ReputationServiceImpl reputationService;

    private JpaUser user;
    private ReputationUpdateRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new JpaUser();
        user.setId(1L);
        user.setPoints(0);

        request = new ReputationUpdateRequest();
        request.setUserId(1L);
    }

    private void mockUserRepositoryAndMapper(int points, String action) {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(reputationMapper.toResponse(user, action, points)).thenReturn(new ReputationResponse());
    }

    @Test
    void testUpdateReputationForNewQuestion() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_NEW_QUESTION, "New Question Posted");
        ReputationResponse response = reputationService.updateReputationForNewQuestion(request);
        assertNotNull(response);
    }

    @Test
    void testUpdateReputationForQuestionWhenDeleted() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_QUESTION_WHEN_DELETED, "Question Deleted");
        assertTrue(reputationService.updateReputationForQuestionWhenQuestionIsDeleted(request));
    }

    @Test
    void testUpdateReputationForNewAnswer() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_NEW_ANSWER, "New Answer Posted");
        ReputationResponse response = reputationService.updateReputationForNewAnswer(request);
        assertNotNull(response);
    }

    @Test
    void testUpdateReputationForAnswerWhenDeleted() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_ANSWER_WHEN_DELETED, "Answer Deleted");
        assertTrue(reputationService.updateReputationForAnswerWhenAnswerIsDeleted(request));
    }

    @Test
    void testUpdateReputationForUpvote() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_UPVOTE_RECEIVED, "Upvote Received");
        ReputationResponse response = reputationService.updateReputationForUpvote(request);
        assertNotNull(response);
    }

    @Test
    void testUpdateReputationForUpvoteRemoved() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_DOWNVOTE_RECEIVED, "Upvote Removed");
        ReputationResponse response = reputationService.updateReputationForUpvoteRemoved(request);
        assertNotNull(response);
    }

    @Test
    void testUpdateReputationForUpvoteGivenRemoved() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_DOWNVOTE_GIVEN, "Upvote Given Removed");
        ReputationResponse response = reputationService.updateReputationForUpvoteGivenRemoved(request);
        assertNotNull(response);
    }

    @Test
    void testUpdateReputationForDownvote() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_DOWNVOTE_RECEIVED, "Downvote Received");
        ReputationResponse response = reputationService.updateReputationForDownvote(request);
        assertNotNull(response);
    }

    @Test
    void testUpdateReputationForUpvoteGiven() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_UPVOTE_GIVEN, "Upvote Given");
        ReputationResponse response = reputationService.updateReputationForUpvoteGiven(request);
        assertNotNull(response);
    }

    @Test
    void testUpdateReputationForDownvoteGiven() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_DOWNVOTE_GIVEN, "Downvote Given");
        ReputationResponse response = reputationService.updateReputationForDownvoteGiven(request);
        assertNotNull(response);
    }

    @Test
    void testUpdateReputationForBestAnswer() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_BEST_ANSWER, "Best Answer Awarded");
        ReputationResponse response = reputationService.updateReputationForBestAnswer(request);
        assertNotNull(response);
    }

    @Test
    void testUpdateReputationForBestAnswerIsDeleted() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_BEST_ANSWER_WHEN_DELETED, "Best Answer Removed");
        ReputationResponse response = reputationService.updateReputationForBestAnswerIsDeleted(request);
        assertNotNull(response);
    }

    @Test
    void testUpdateReputationForConsecutiveActivity() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_CONSECUTIVE_ACTIVITY, "Consecutive Activity Points");
        ReputationResponse response = reputationService.updateReputationForConsecutiveActivity(request);
        assertNotNull(response);
    }

    @Test
    void testUpdateReputationForNewComment() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_NEW_COMMENT, "New Comment Posted");
        ReputationResponse response = reputationService.updateReputationForNewComment(request);
        assertNotNull(response);
    }

    @Test
    void testUpdateReputationForCommentWhenDeleted() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_COMMENT_WHEN_DELETED, "Comment Deleted");
        assertTrue(reputationService.updateReputationForCommentWhenCommentIsDeleted(request));
    }

    @Test
    void testUpdateReputationForUpvoteComment() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_UPVOTE_COMMENT, "Comment Upvoted");
        ReputationResponse response = reputationService.updateReputationForUpvoteComment(request);
        assertNotNull(response);
    }

    @Test
    void testUpdateReputationForDownvoteComment() {
        mockUserRepositoryAndMapper(ReputationConstants.POINTS_DOWNVOTE_COMMENT, "Comment Downvoted");
        ReputationResponse response = reputationService.updateReputationForDownvoteComment(request);
        assertNotNull(response);
    }

    @Test
    void testGetCurrentReputation() {
        mockUserRepositoryAndMapper(0, "Current Reputation");
        ReputationResponse response = reputationService.getCurrentReputation(1L);
        assertNotNull(response);
    }

    @Test
    void testUpdateReputation_userNotFound() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> reputationService.updateReputationForNewQuestion(request));
    }

    @Test
    void testReputationConstantsInstantiation() throws NoSuchMethodException {
        Constructor<ReputationConstants> constructor = ReputationConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThrows(UnsupportedOperationException.class, () -> instantiateConstructor(constructor));
    }

    @Test
    void testGetCurrentReputation_userNotFound() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> reputationService.getCurrentReputation(1L));
    }

    @Test
    void testUpdateReputation_exceptionHandling() {
        when(userRepository.findById(any())).thenThrow(new RuntimeException("Database error"));
        assertThrows(IllegalArgumentException.class, () -> reputationService.updateReputationForNewQuestion(request));
    }

    private void instantiateConstructor(Constructor<ReputationConstants> constructor) {
        try {
            constructor.newInstance();
        } catch (InvocationTargetException e) {
            // Re-throw the cause if it's a RuntimeException; otherwise, wrap it
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
