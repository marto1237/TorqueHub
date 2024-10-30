package torquehub.torquehub.business.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import torquehub.torquehub.business.interfaces.NotificationService;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.domain.ReputationConstants;
import torquehub.torquehub.domain.model.jpa_models.*;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;
import torquehub.torquehub.persistence.repository.VoteRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoteServiceImplTest {

    @InjectMocks
    private VoteServiceImpl voteService;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private ReputationService reputationService;

    @Mock
    private NotificationService notificationService;

    private JpaUser testUser;
    private JpaUser contentOwner;
    private JpaQuestion testQuestion;
    private JpaAnswer testAnswer;
    private JpaComment testComment;
    private JpaVote testVote;
    private ReputationResponse testReputationResponse;

    @BeforeEach
    void setUp() {
        testUser = JpaUser.builder()
                .id(1L)
                .username("testUser")
                .build();

        contentOwner = JpaUser.builder()
                .id(2L)
                .username("contentOwner")
                .build();

        testQuestion = JpaQuestion.builder()
                .id(1L)
                .title("Test Question")
                .jpaUser(contentOwner)
                .votes(0)
                .build();

        testAnswer = JpaAnswer.builder()
                .id(1L)
                .jpaUser(contentOwner)
                .votes(0)
                .build();

        testComment = JpaComment.builder()
                .id(1L)
                .jpaUser(contentOwner)
                .text("Test Comment")
                .votes(0)
                .build();

        testVote = JpaVote.builder()
                .jpaUser(testUser)
                .jpaQuestion(testQuestion)
                .upvote(true)
                .build();

        testReputationResponse = ReputationResponse.builder()
                .userId(contentOwner.getId())
                .updatedReputationPoints(10)
                .build();
    }

    @Test
    void shouldIncrementVoteAndUpdateReputation_WhenNewUpvoteIsCreatedForQuestion() {
        when(voteRepository.findByUserAndJpaQuestion(testUser, testQuestion))
                .thenReturn(Optional.empty());
        when(voteRepository.save(any(JpaVote.class))).thenReturn(testVote);
        when(reputationService.updateReputationForUpvote(any()))
                .thenReturn(testReputationResponse);
        when(reputationService.updateReputationForUpvoteGiven(any()))
                .thenReturn(testReputationResponse);

        ReputationResponse response = voteService.handleUpvote(testUser, testQuestion);

        assertNotNull(response);
        assertEquals(testReputationResponse.getUserId(), response.getUserId());
        assertEquals(testReputationResponse.getUpdatedReputationPoints(), response.getUpdatedReputationPoints());
        assertEquals(1, testQuestion.getVotes());

        verify(voteRepository).save(any(JpaVote.class));
        verify(reputationService).updateReputationForUpvote(any());
        verify(reputationService).updateReputationForUpvoteGiven(any());
        verify(notificationService).notifyUserAboutQuestionVote(any());
    }

    @Test
    void shouldDecrementVoteAndUpdateReputation_WhenNewDownvoteIsCreatedForQuestion() {
        when(voteRepository.findByUserAndJpaQuestion(testUser, testQuestion))
                .thenReturn(Optional.empty());
        when(voteRepository.save(any(JpaVote.class))).thenReturn(testVote);
        when(reputationService.updateReputationForDownvote(any()))
                .thenReturn(testReputationResponse);
        when(reputationService.updateReputationForDownvoteGiven(any()))
                .thenReturn(testReputationResponse);

        ReputationResponse response = voteService.handleDownvote(testUser, testQuestion);

        assertNotNull(response);
        assertEquals(testReputationResponse.getUserId(), response.getUserId());
        assertEquals(testReputationResponse.getUpdatedReputationPoints(), response.getUpdatedReputationPoints());
        assertEquals(-1, testQuestion.getVotes());

        verify(voteRepository).save(any(JpaVote.class));
        verify(reputationService).updateReputationForDownvote(any());
        verify(reputationService).updateReputationForDownvoteGiven(any());
        verify(notificationService, never()).notifyUserAboutQuestionVote(any());
    }

    @Test
    void shouldNotChangeVoteOrReputation_WhenSameVoteTypeExistsForQuestion() {
        testVote.setUpvote(true);
        when(voteRepository.findByUserAndJpaQuestion(testUser, testQuestion))
                .thenReturn(Optional.of(testVote));
        when(reputationService.getCurrentReputation(testUser.getId()))
                .thenReturn(testReputationResponse);

        ReputationResponse response = voteService.handleUpvote(testUser, testQuestion);

        assertNotNull(response);

        verify(voteRepository, never()).save(any(JpaVote.class));
        verify(reputationService, never()).updateReputationForUpvote(any());
        verify(reputationService, never()).updateReputationForUpvoteGiven(any());
        verify(notificationService, never()).notifyUserAboutQuestionVote(any());
    }

    @Test
    void shouldUpdateVoteAndReputation_WhenVoteTypeChangesForQuestion() {

        testVote.setUpvote(false);
        when(voteRepository.findByUserAndJpaQuestion(testUser, testQuestion))
                .thenReturn(Optional.of(testVote));
        when(voteRepository.save(any(JpaVote.class))).thenReturn(testVote);
        when(reputationService.updateReputationForUpvote(any()))
                .thenReturn(testReputationResponse);
        when(reputationService.updateReputationForUpvoteGiven(any()))
                .thenReturn(testReputationResponse);

        ReputationResponse response = voteService.handleUpvote(testUser, testQuestion);

        assertNotNull(response);
        assertTrue(testVote.isUpvote());
        assertEquals(2, testQuestion.getVotes());

        verify(voteRepository).save(testVote);
        verify(reputationService).updateReputationForUpvote(any());
        verify(reputationService).updateReputationForUpvoteGiven(any());
        verify(notificationService).notifyUserAboutQuestionVote(any());
    }

    @Test
    void shouldIncrementVoteAndUpdateReputation_WhenNewUpvoteIsCreatedForAnswer() {
        when(voteRepository.findByUserAndJpaAnswer(testUser, testAnswer))
                .thenReturn(Optional.empty());
        when(voteRepository.save(any(JpaVote.class))).thenReturn(testVote);
        when(reputationService.updateReputationForUpvote(any()))
                .thenReturn(testReputationResponse);
        when(reputationService.updateReputationForUpvoteGiven(any()))
                .thenReturn(testReputationResponse);

        ReputationResponse response = voteService.handleUpvoteForAnswer(testUser, testAnswer);

        assertNotNull(response);
        assertEquals(1, testAnswer.getVotes());

        verify(voteRepository).save(any(JpaVote.class));
        verify(reputationService).updateReputationForUpvote(any());
        verify(reputationService).updateReputationForUpvoteGiven(any());
        verify(notificationService).notifyUserAboutAnswerVote(any());
    }

    @Test
    void shouldIncrementVoteAndUpdateReputation_WhenNewUpvoteIsCreatedForComment() {
        when(voteRepository.findByUserAndJpaComment(testUser, testComment))
                .thenReturn(Optional.empty());
        when(voteRepository.save(any(JpaVote.class))).thenReturn(testVote);
        when(reputationService.updateReputationForUpvote(any()))
                .thenReturn(testReputationResponse);
        when(reputationService.updateReputationForUpvoteGiven(any()))
                .thenReturn(testReputationResponse);

        ReputationResponse response = voteService.handleUpvoteForComment(testUser, testComment);

        assertNotNull(response);
        assertEquals(1, testComment.getVotes());

        verify(voteRepository).save(any(JpaVote.class));
        verify(reputationService).updateReputationForUpvote(any());
        verify(reputationService).updateReputationForUpvoteGiven(any());
        verify(notificationService).notifyUserAboutCommentVote(any());
    }

    @Test
    void shouldUseCorrectReputationPointValuesWhenUpdatingReputation() {
        when(voteRepository.findByUserAndJpaQuestion(testUser, testQuestion))
                .thenReturn(Optional.empty());
        when(voteRepository.save(any(JpaVote.class))).thenReturn(testVote);

        voteService.handleUpvote(testUser, testQuestion);

        verify(reputationService).updateReputationForUpvote(
                argThat(request ->
                        request.getPoints() == ReputationConstants.POINTS_UPVOTE_RECEIVED
                )
        );
        verify(reputationService).updateReputationForUpvoteGiven(
                argThat(request ->
                        request.getPoints() == ReputationConstants.POINTS_UPVOTE_GIVEN
                )
        );
    }
    @Test
    void shouldHandleNewUpvoteAndIncrementVoteCount_WhenVoteIsNewForQuestion() {
        when(voteRepository.findByUserAndJpaQuestion(testUser, testQuestion))
                .thenReturn(Optional.empty());
        when(voteRepository.save(any(JpaVote.class))).thenReturn(testVote);
        when(reputationService.updateReputationForUpvote(any()))
                .thenReturn(testReputationResponse);
        when(reputationService.updateReputationForUpvoteGiven(any()))
                .thenReturn(testReputationResponse);

        ReputationResponse response = voteService.handleUpvote(testUser, testQuestion);

        assertNotNull(response);
        assertEquals(1, testQuestion.getVotes());
        verify(notificationService).notifyUserAboutQuestionVote(any());
    }

    @Test
    void shouldDecrementVoteAndUpdateReputationWhenNewDownvoteIsCreatedForAnswer() {
        when(voteRepository.findByUserAndJpaQuestion(testUser, testQuestion))
                .thenReturn(Optional.empty());
        when(voteRepository.save(any(JpaVote.class))).thenReturn(testVote);
        when(reputationService.updateReputationForDownvote(any()))
                .thenReturn(testReputationResponse);
        when(reputationService.updateReputationForDownvoteGiven(any()))
                .thenReturn(testReputationResponse);

        ReputationResponse response = voteService.handleDownvote(testUser, testQuestion);

        assertNotNull(response);
        assertEquals(-1, testQuestion.getVotes());
        verify(notificationService, never()).notifyUserAboutQuestionVote(any());
    }

    @Test
    void shouldChangeVoteAndReputation_WhenExistingUpvoteIsChangedToDownvoteForQuestion() {
        testVote.setUpvote(true);
        when(voteRepository.findByUserAndJpaQuestion(testUser, testQuestion))
                .thenReturn(Optional.of(testVote));
        when(voteRepository.save(any(JpaVote.class))).thenReturn(testVote);
        when(reputationService.updateReputationForDownvote(any()))
                .thenReturn(testReputationResponse);
        when(reputationService.updateReputationForDownvoteGiven(any()))
                .thenReturn(testReputationResponse);

        ReputationResponse response = voteService.handleDownvote(testUser, testQuestion);

        assertNotNull(response);
        assertFalse(testVote.isUpvote());
        assertEquals(-2, testQuestion.getVotes());
    }


    @Test
    void shouldDecrementVoteAndUpdateReputation_WhenNewDownvoteIsCreatedForAnswer() {
        when(voteRepository.findByUserAndJpaAnswer(testUser, testAnswer))
                .thenReturn(Optional.empty());
        when(voteRepository.save(any(JpaVote.class))).thenReturn(testVote);
        when(reputationService.updateReputationForDownvote(any()))
                .thenReturn(testReputationResponse);
        when(reputationService.updateReputationForDownvoteGiven(any()))
                .thenReturn(testReputationResponse);

        ReputationResponse response = voteService.handleDownvoteForAnswer(testUser, testAnswer);

        assertNotNull(response);
        assertEquals(-1, testAnswer.getVotes());
        verify(notificationService, never()).notifyUserAboutAnswerVote(any());
    }

    @Test
    void shouldNotChangeVoteOrReputation_WhenSameVoteTypeExistsForAnswer() {
        JpaVote answerVote = JpaVote.builder()
                .jpaUser(testUser)
                .jpaAnswer(testAnswer)
                .upvote(true)
                .build();

        when(voteRepository.findByUserAndJpaAnswer(testUser, testAnswer))
                .thenReturn(Optional.of(answerVote));
        when(reputationService.getCurrentReputation(testUser.getId()))
                .thenReturn(testReputationResponse);

        ReputationResponse response = voteService.handleUpvoteForAnswer(testUser, testAnswer);

        assertNotNull(response);
        verify(voteRepository, never()).save(any(JpaVote.class));
        verify(notificationService, never()).notifyUserAboutAnswerVote(any());
    }

    @Test
    void shouldChangeVoteAndReputation_WhenVoteTypeChangesForAnswer() {
        JpaVote answerVote = JpaVote.builder()
                .jpaUser(testUser)
                .jpaAnswer(testAnswer)
                .upvote(false)
                .build();

        when(voteRepository.findByUserAndJpaAnswer(testUser, testAnswer))
                .thenReturn(Optional.of(answerVote));
        when(voteRepository.save(any(JpaVote.class))).thenReturn(answerVote);
        when(reputationService.updateReputationForUpvote(any()))
                .thenReturn(testReputationResponse);
        when(reputationService.updateReputationForUpvoteGiven(any()))
                .thenReturn(testReputationResponse);

        ReputationResponse response = voteService.handleUpvoteForAnswer(testUser, testAnswer);

        assertNotNull(response);
        assertTrue(answerVote.isUpvote());
        assertEquals(2, testAnswer.getVotes());
        verify(notificationService).notifyUserAboutAnswerVote(any());
    }


    @Test
    void shouldDecrementVoteAndUpdateReputation_WhenNewDownvoteIsCreatedForComment() {
        when(voteRepository.findByUserAndJpaComment(testUser, testComment))
                .thenReturn(Optional.empty());
        when(voteRepository.save(any(JpaVote.class))).thenReturn(testVote);
        when(reputationService.updateReputationForDownvote(any()))
                .thenReturn(testReputationResponse);
        when(reputationService.updateReputationForDownvoteGiven(any()))
                .thenReturn(testReputationResponse);

        ReputationResponse response = voteService.handleDownvoteForComment(testUser, testComment);

        assertNotNull(response);
        assertEquals(-1, testComment.getVotes());
        verify(notificationService, never()).notifyUserAboutCommentVote(any());
    }

    @Test
    void shouldNotChangeVoteOrReputation_WhenSameVoteTypeExistsForComment() {
        JpaVote commentVote = JpaVote.builder()
                .jpaUser(testUser)
                .jpaComment(testComment)
                .upvote(true)
                .build();

        when(voteRepository.findByUserAndJpaComment(testUser, testComment))
                .thenReturn(Optional.of(commentVote));
        when(reputationService.getCurrentReputation(testUser.getId()))
                .thenReturn(testReputationResponse);

        ReputationResponse response = voteService.handleUpvoteForComment(testUser, testComment);

        assertNotNull(response);
        verify(voteRepository, never()).save(any(JpaVote.class));
        verify(notificationService, never()).notifyUserAboutCommentVote(any());
    }

    @Test
    void shouldChangeVoteAndReputation_WhenVoteTypeChangesForComment() {
        JpaVote commentVote = JpaVote.builder()
                .jpaUser(testUser)
                .jpaComment(testComment)
                .upvote(false)
                .build();

        when(voteRepository.findByUserAndJpaComment(testUser, testComment))
                .thenReturn(Optional.of(commentVote));
        when(voteRepository.save(any(JpaVote.class))).thenReturn(commentVote);
        when(reputationService.updateReputationForUpvote(any()))
                .thenReturn(testReputationResponse);
        when(reputationService.updateReputationForUpvoteGiven(any()))
                .thenReturn(testReputationResponse);

        ReputationResponse response = voteService.handleUpvoteForComment(testUser, testComment);

        assertNotNull(response);
        assertTrue(commentVote.isUpvote());
        assertEquals(2, testComment.getVotes());
        verify(notificationService).notifyUserAboutCommentVote(any());
    }
}