package torquehub.torquehub.business.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import torquehub.torquehub.controllers.websocketcontrollers.WebSocketNotificationController;
import torquehub.torquehub.domain.mapper.NotificationMapper;
import torquehub.torquehub.domain.model.jpa_models.*;
import torquehub.torquehub.domain.request.notification_dtos.*;
import torquehub.torquehub.domain.request.vote_dtos.VoteAnswerNotificationRequest;
import torquehub.torquehub.domain.request.vote_dtos.VoteCommentNotificationRequest;
import torquehub.torquehub.domain.request.vote_dtos.VoteQuestionNotificationRequest;
import torquehub.torquehub.domain.response.notification_dtos.NotificationResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaAnswerRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaFollowRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaNotificationRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaQuestionRepository;
import torquehub.torquehub.persistence.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EnableTransactionManagement
class NotificationServiceImplTest {

    @Mock
    private JpaNotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JpaAnswerRepository answerRepository;

    @Mock
    private WebSocketNotificationController webSocketNotificationController;

    @Mock
    private JpaQuestionRepository questionRepository;

    @Mock
    private JpaFollowRepository followRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void testCreateNotification_Success() {
        JpaUser user = new JpaUser();
        user.setId(1L);
        JpaUser voter = new JpaUser();
        voter.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(voter));

        CreateNotificationRequest request = new CreateNotificationRequest(1L, 2L, 10,"You received a vote");
        JpaNotification jpaNotification = new JpaNotification();
        when(notificationRepository.save(any(JpaNotification.class))).thenReturn(jpaNotification);

        NotificationResponse expectedResponse = new NotificationResponse();
        when(notificationMapper.toResponse(jpaNotification)).thenReturn(expectedResponse);

        Optional<NotificationResponse> response = notificationService.createNotification(request);

        assertTrue(response.isPresent());
        assertEquals(expectedResponse, response.get());
    }

    @Test
    void testCreateNotification_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        CreateNotificationRequest request = new CreateNotificationRequest(1L, 2L, 10,"You received a vote");

        assertThrows(IllegalArgumentException.class, () -> notificationService.createNotification(request));
    }

    @Test
    void testCreateNotification_VoterNotFound() {
        JpaUser user = new JpaUser();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        CreateNotificationRequest request = new CreateNotificationRequest(1L, 2L, 10,"You received a vote");
        assertThrows(IllegalArgumentException.class, () -> notificationService.createNotification(request));
    }

    // Test for notifyAnswerOwner
    @Test
    void testNotifyAnswerOwner_Success() {
        JpaUser owner = new JpaUser();
        owner.setId(1L);
        JpaUser voter = new JpaUser();
        voter.setId(2L);
        JpaAnswer answer = new JpaAnswer();
        answer.setJpaUser(owner);
        answer.setJpaQuestion(new JpaQuestion());
        answer.setJpaUser(voter);

        ReputationResponse reputationResponse = new ReputationResponse();
        reputationResponse.setUpdatedReputationPoints(100);

        when(notificationRepository.save(any(JpaNotification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyAnswerOwner(owner, answer, true, reputationResponse);

        verify(notificationRepository, times(1)).save(any(JpaNotification.class));
    }



    @Test
    @Transactional
    void testNotifyUserAboutQuestionVote_SelfVote() {
        JpaUser questionCreator = new JpaUser();
        questionCreator.setId(1L);
        JpaQuestion question = new JpaQuestion();
        question.setJpaUser(questionCreator);

        VoteQuestionNotificationRequest request = new VoteQuestionNotificationRequest(1L, "User 1 upvoted their question", 1L, 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(questionCreator));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        Optional<NotificationResponse> response = notificationService.notifyUserAboutQuestionVote(request);

        assertFalse(response.isPresent());
        verify(notificationRepository, times(0)).save(any(JpaNotification.class));
    }


    @Test
    void testMarkAsRead_Success() {
        JpaNotification notification = new JpaNotification();
        notification.setId(1L);
        when(notificationRepository.findById(1L)).thenReturn(notification);

        boolean result = notificationService.markAsRead(1L);

        assertTrue(result);
        assertTrue(notification.isRead());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void testMarkAsRead_NotFound() {
        when(notificationRepository.findById(1L)).thenReturn(null);

        boolean result = notificationService.markAsRead(1L);

        assertFalse(result);
        verify(notificationRepository, times(0)).save(any(JpaNotification.class));
    }

    @Test
    void testMarkAllAsRead_Success() {
        JpaNotification notification1 = new JpaNotification();
        notification1.setRead(false);
        JpaNotification notification2 = new JpaNotification();
        notification2.setRead(false);

        List<JpaNotification> notifications = List.of(notification1, notification2);

        when(notificationRepository.findByJpaUserIdAndIsReadFalse(1L)).thenReturn(notifications);

        boolean result = notificationService.markAllAsRead(1L);

        assertTrue(result);
        verify(notificationRepository, times(1)).saveAll(notifications);
    }

    @Test
    void testMarkAllAsRead_NoUnreadNotifications() {
        when(notificationRepository.findByJpaUserIdAndIsReadFalse(1L)).thenReturn(new ArrayList<>());

        boolean result = notificationService.markAllAsRead(1L);

        assertFalse(result);
        verify(notificationRepository, times(0)).saveAll(anyList());
    }

    @Test
    void testNotifyUserAboutPoints_Success() {
        JpaUser recipient = new JpaUser();
        recipient.setId(1L);
        JpaUser voter = new JpaUser();
        voter.setId(2L);

        PointsNotificationRequest pointsRequest = new PointsNotificationRequest(recipient, 10, "You received 10 points", voter);

        when(userRepository.findById(1L)).thenReturn(Optional.of(recipient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(voter));
        JpaNotification jpaNotification = new JpaNotification();
        when(notificationRepository.save(any(JpaNotification.class))).thenReturn(jpaNotification);

        NotificationResponse notificationResponse = new NotificationResponse();
        when(notificationMapper.toResponse(jpaNotification)).thenReturn(notificationResponse);

        Optional<NotificationResponse> response = notificationService.notifyUserAboutPoints(pointsRequest);

        assertTrue(response.isPresent());
        assertEquals(notificationResponse, response.get());
        verify(notificationRepository, times(1)).save(any(JpaNotification.class));
    }


    @Test
    void testNotifyUserAboutAnswerVote_Success() {
        // Arrange: Create answer creator, voter, and answer objects
        JpaUser answerCreator = new JpaUser();
        answerCreator.setId(1L);

        JpaUser voter = new JpaUser();
        voter.setId(2L);

        JpaQuestion question = new JpaQuestion();
        question.setId(3L); // Ensure this ID is consistent
        question.setJpaUser(answerCreator);

        JpaAnswer answer = new JpaAnswer();
        answer.setId(1L); // This matches the answerId in the request
        answer.setJpaQuestion(question); // Link the question to the answer
        answer.setJpaUser(answerCreator); // Set the answer creator

        VoteAnswerNotificationRequest request = new VoteAnswerNotificationRequest(
                1L,
                "User 2 upvoted your answer",
                2L,
                1L
        );

        // Mock static method in TransactionSynchronizationManager
        try (MockedStatic<TransactionSynchronizationManager> transactionSynchronizationManagerMock = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            transactionSynchronizationManagerMock.when(TransactionSynchronizationManager::isActualTransactionActive).thenReturn(true);

            // Set up mocks to return expected values
            when(userRepository.findById(1L)).thenReturn(Optional.of(answerCreator));
            when(userRepository.findById(2L)).thenReturn(Optional.of(voter));
            when(answerRepository.findById(1L)).thenReturn(Optional.of(answer)); // Mock fetching the answer

            JpaNotification jpaNotification = new JpaNotification();
            when(notificationRepository.save(any(JpaNotification.class))).thenReturn(jpaNotification);

            NotificationResponse notificationResponse = new NotificationResponse();
            when(notificationMapper.toResponse(jpaNotification)).thenReturn(notificationResponse);

            // Act: Call the method under test
            Optional<NotificationResponse> response = notificationService.notifyUserAboutAnswerVote(request);

            // Assert: Verify the outcome
            assertTrue(response.isPresent());
            assertEquals(notificationResponse, response.get());
            verify(notificationRepository, times(1)).save(any(JpaNotification.class));
        }
    }

    @Test
    void testNotifyAnswerOwnerForNewComment_Success() {
        // Arrange
        JpaUser answerOwner = new JpaUser();
        answerOwner.setId(1L);
        JpaUser commenter = new JpaUser();
        commenter.setId(2L);

        CreateCommentAnswerRequest createCommentAnswerRequest = new CreateCommentAnswerRequest(1L, 2L, "New comment added", 5);

        // Mock static method in TransactionSynchronizationManager
        try (MockedStatic<TransactionSynchronizationManager> transactionSynchronizationManagerMock = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            transactionSynchronizationManagerMock.when(TransactionSynchronizationManager::isActualTransactionActive).thenReturn(true);

            when(userRepository.findById(1L)).thenReturn(Optional.of(answerOwner));
            when(userRepository.findById(2L)).thenReturn(Optional.of(commenter));

            JpaNotification jpaNotification = new JpaNotification();
            when(notificationRepository.save(any(JpaNotification.class))).thenReturn(jpaNotification);

            NotificationResponse notificationResponse = new NotificationResponse();
            when(notificationMapper.toResponse(jpaNotification)).thenReturn(notificationResponse);

            // Act
            Optional<NotificationResponse> response = notificationService.notifyAnswerOwnerForNewComment(createCommentAnswerRequest);

            // Assert
            assertTrue(response.isPresent());
            assertEquals(notificationResponse, response.get());
            verify(notificationRepository, times(1)).save(any(JpaNotification.class));
        }
    }

    @Test
    void testNotifyUserAboutCommentVote_Success() {
        // Arrange
        JpaUser commentOwner = new JpaUser();
        commentOwner.setId(1L);
        JpaUser voter = new JpaUser();
        voter.setId(2L);

        VoteCommentNotificationRequest request = new VoteCommentNotificationRequest(1L, "User 2 upvoted your comment", 1L, 2L);

        // Mock static method in TransactionSynchronizationManager
        try (MockedStatic<TransactionSynchronizationManager> transactionSynchronizationManagerMock = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            transactionSynchronizationManagerMock.when(TransactionSynchronizationManager::isActualTransactionActive).thenReturn(true);

            // Standard mock setups for repository methods
            when(userRepository.findById(1L)).thenReturn(Optional.of(commentOwner));
            when(userRepository.findById(2L)).thenReturn(Optional.of(voter));

            JpaNotification jpaNotification = new JpaNotification();
            when(notificationRepository.save(any(JpaNotification.class))).thenReturn(jpaNotification);

            NotificationResponse notificationResponse = new NotificationResponse();
            when(notificationMapper.toResponse(jpaNotification)).thenReturn(notificationResponse);

            // Act
            Optional<NotificationResponse> response = notificationService.notifyUserAboutCommentVote(request);

            // Assert
            assertTrue(response.isPresent());
            assertEquals(notificationResponse, response.get());
            verify(notificationRepository, times(1)).save(any(JpaNotification.class));
        }
    }


    @Test
    void testFindTop5ByUserIdUnread() {
        List<JpaNotification> notifications = List.of(
                new JpaNotification(), new JpaNotification(), new JpaNotification(),
                new JpaNotification(), new JpaNotification()
        );

        // Wrap the notifications list in a PageImpl to simulate a pageable response
        PageImpl<JpaNotification> notificationPage = new PageImpl<>(notifications);

        when(notificationRepository.findByJpaUserIdAndIsReadFalse(eq(1L), any(Pageable.class)))
                .thenReturn(notificationPage);

        List<NotificationResponse> expectedResponses = notifications.stream()
                .map(notification -> new NotificationResponse())
                .toList();

        for (int i = 0; i < notifications.size(); i++) {
            when(notificationMapper.toResponse(notifications.get(i))).thenReturn(expectedResponses.get(i));
        }

        List<NotificationResponse> responses = notificationService.findTop5ByUserIdUnread(1L);

        assertEquals(5, responses.size());
        verify(notificationRepository, times(1)).findByJpaUserIdAndIsReadFalse(eq(1L), any(Pageable.class));
    }

    @Test
    void testFindByUserId() {
        JpaNotification notification1 = new JpaNotification();
        JpaNotification notification2 = new JpaNotification();

        Page<JpaNotification> notificationPage = new PageImpl<>(List.of(notification1, notification2));

        when(notificationRepository.findByJpaUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(notificationPage);

        NotificationResponse response1 = new NotificationResponse();
        NotificationResponse response2 = new NotificationResponse();
        when(notificationMapper.toResponse(notification1)).thenReturn(response1);
        when(notificationMapper.toResponse(notification2)).thenReturn(response2);

        Page<NotificationResponse> resultPage = notificationService.findByUserId(1L, Pageable.ofSize(2));

        assertEquals(2, resultPage.getContent().size());
        assertEquals(response1, resultPage.getContent().get(0));
        assertEquals(response2, resultPage.getContent().get(1));
    }

    @Test
    void testNotifyFollowersAboutNewAnswer() {
        // Arrange: Create necessary objects and set up the request
        NewAnswerNotificationRequest request = new NewAnswerNotificationRequest(1L, 1L, 2L, "New answer notification message");

        JpaUser userWhoAnswered = new JpaUser();
        userWhoAnswered.setId(2L);

        JpaUser followerUser = new JpaUser();
        followerUser.setId(3L);

        JpaFollow follower = new JpaFollow();
        follower.setJpaUser(followerUser);

        JpaQuestion question = new JpaQuestion();
        question.setId(1L);

        List<JpaFollow> followers = List.of(follower);

        // Set up mocks to return expected values
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(userRepository.findById(2L)).thenReturn(Optional.of(userWhoAnswered));
        when(followRepository.findByQuestionIdAndMutedFalse(1L)).thenReturn(followers);
        doNothing().when(notificationRepository).saveAll(anyList());

        // Mock static method in TransactionSynchronizationManager
        try (MockedStatic<TransactionSynchronizationManager> transactionSynchronizationManagerMock = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            transactionSynchronizationManagerMock.when(TransactionSynchronizationManager::isActualTransactionActive).thenReturn(true);

            // Act: Call the method under test
            Optional<NotificationResponse> result = notificationService.notifyFollowersAboutNewAnswer(request);

            // Assert: Verify the outcome
            assertTrue(result.isEmpty(), "Expected Optional.empty() as the result");
            verify(notificationRepository, times(1)).saveAll(anyList());


        }
    }


    @Test
    void testNotifyAnswerFollowersAboutNewComment() {
        NewCommentOnAnswerNotificationRequest request = new NewCommentOnAnswerNotificationRequest(1L, 1L, 2L, "New comment on answer message");

        // Arrange: Setup test data
        JpaUser userWhoCommented = new JpaUser();
        userWhoCommented.setId(2L);

        JpaUser followerUser = new JpaUser();
        followerUser.setId(4L);

        JpaFollow follower = new JpaFollow();
        follower.setJpaUser(followerUser);

        List<JpaFollow> answerFollowers = List.of(follower);

        try (MockedStatic<TransactionSynchronizationManager> transactionSynchronizationManagerMock = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            transactionSynchronizationManagerMock.when(TransactionSynchronizationManager::isActualTransactionActive).thenReturn(true);

            // Setting up mocks to return expected values
            when(userRepository.findById(2L)).thenReturn(Optional.of(userWhoCommented));
            when(followRepository.findByAnswerIdAndMutedFalse(1L)).thenReturn(answerFollowers);

            JpaNotification jpaNotification = new JpaNotification();
            when(notificationRepository.save(any(JpaNotification.class))).thenReturn(jpaNotification);

            // Optional: Add mapping of JpaNotification to NotificationResponse if necessary
            NotificationResponse notificationResponse = new NotificationResponse();
            when(notificationMapper.toResponse(jpaNotification)).thenReturn(notificationResponse);

            // Act: Call the method under test
            Optional<NotificationResponse> result = notificationService.notifyAnswerFollowersAboutNewComment(request);

            // Assert: Verify the outcome
            assertTrue(result.isEmpty(), "Expected Optional.empty() as the result");
            verify(notificationRepository, times(1)).saveAll(anyList());
        }
    }


}
