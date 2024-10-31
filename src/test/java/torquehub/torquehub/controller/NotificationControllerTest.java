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
import torquehub.torquehub.controllers.NotificationController;
import torquehub.torquehub.domain.response.notification_dtos.NotificationResponse;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetLatestUnreadNotifications() {
        Long userId = 1L;
        List<NotificationResponse> unreadNotifications = Collections.singletonList(new NotificationResponse());

        when(notificationService.findTop5ByUserIdUnread(userId)).thenReturn(unreadNotifications);

        ResponseEntity<List<NotificationResponse>> response = notificationController.getLatestUnreadNotifications(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(unreadNotifications, response.getBody());
        verify(notificationService, times(1)).findTop5ByUserIdUnread(userId);
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
}
