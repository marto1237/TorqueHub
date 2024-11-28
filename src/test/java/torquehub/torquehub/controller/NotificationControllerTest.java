package torquehub.torquehub.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import torquehub.torquehub.business.interfaces.NotificationService;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.controllers.NotificationController;
import torquehub.torquehub.domain.response.notification_dtos.DetailNotificationResponse;
import torquehub.torquehub.domain.response.notification_dtos.NotificationResponse;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private TokenUtil tokenUtil;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetLatestUnreadNotifications() {
        Long userId = 1L;
        long totalUnreadCount = 10L;

        DetailNotificationResponse detailNotification = DetailNotificationResponse.builder()
                .id(1L)
                .message("Test Notification")
                .userId(userId)
                .voterId(2L)
                .points(5)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .count(totalUnreadCount)
                .build();

        List<DetailNotificationResponse> unreadNotifications = Collections.singletonList(detailNotification);

        when(notificationService.findTop5UnreadWithCount(userId)).thenReturn(unreadNotifications);

        ResponseEntity<List<DetailNotificationResponse>> response = notificationController.getLatestUnreadNotifications(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(unreadNotifications, response.getBody());
        verify(notificationService, times(1)).findTop5UnreadWithCount(userId);
    }

    @Test
    void testGetAllNotifications() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        Page<NotificationResponse> notificationsPage = new PageImpl<>(Collections.singletonList(new NotificationResponse()));

        when(notificationService.findByUserId(userId, pageable)).thenReturn(notificationsPage);

        ResponseEntity<Page<NotificationResponse>> response = notificationController.getAllNotifications(userId, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(notificationsPage, response.getBody());
        verify(notificationService, times(1)).findByUserId(userId, pageable);
    }

    @Test
    void testMarkNotificationAsRead() {
        Long notificationId = 1L;

        ResponseEntity<Void> response = notificationController.markNotificationAsRead(notificationId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService, times(1)).markAsRead(notificationId);
    }

    @Test
    void testMarkAllNotificationsAsRead() {
        Long userId = 1L;

        ResponseEntity<Void> response = notificationController.markAllNotificationsAsRead(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService, times(1)).markAllAsRead(userId);
    }
    @Test
    void testGetUserAllNotifications_Success() {
        String token = "valid-token";
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        Page<NotificationResponse> notificationsPage = new PageImpl<>(Collections.singletonList(new NotificationResponse()));

        when(tokenUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(notificationService.findByUserId(userId, pageable)).thenReturn(notificationsPage);

        ResponseEntity<Page<NotificationResponse>> response = notificationController.getUserAllNotifications(token, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(notificationsPage, response.getBody());
        verify(tokenUtil, times(1)).getUserIdFromToken(token);
        verify(notificationService, times(1)).findByUserId(userId, pageable);
    }

    @Test
    void testGetUserAllNotifications_InvalidToken() {
        String token = "invalid-token";

        when(tokenUtil.getUserIdFromToken(token)).thenThrow(new InvalidAccessTokenException("Invalid token"));

        ResponseEntity<Page<NotificationResponse>> response = notificationController.getUserAllNotifications(token, 0, 20);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(null, response.getBody());
        verify(tokenUtil, times(1)).getUserIdFromToken(token);
        verifyNoInteractions(notificationService);
    }

    @Test
    void testGetUserAllNotifications_Exception() {
        String token = "valid-token";
        Long userId = 1L;

        when(tokenUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(notificationService.findByUserId(anyLong(), any(Pageable.class))).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Page<NotificationResponse>> response = notificationController.getUserAllNotifications(token, 0, 20);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(null, response.getBody());
        verify(tokenUtil, times(1)).getUserIdFromToken(token);
        verify(notificationService, times(1)).findByUserId(userId, PageRequest.of(0, 20));
    }
}
