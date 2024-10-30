package torquehub.torquehub.business.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.controllers.websocketcontrollers.WebSocketNotificationController;
import torquehub.torquehub.domain.mapper.NotificationMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaNotification;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.request.notification_dtos.CreateNotificationRequest;
import torquehub.torquehub.domain.request.vote_dtos.VoteQuestionNotificationRequest;
import torquehub.torquehub.domain.response.notification_dtos.NotificationResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaNotificationRepository;
import torquehub.torquehub.persistence.repository.QuestionRepository;
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
    private WebSocketNotificationController webSocketNotificationController;

    @Mock
    private QuestionRepository questionRepository;

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


}