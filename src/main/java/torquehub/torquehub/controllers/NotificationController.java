package torquehub.torquehub.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.NotificationService;
import torquehub.torquehub.domain.response.notification_dtos.DetailNotificationResponse;
import torquehub.torquehub.domain.response.notification_dtos.NotificationResponse;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Fetch the latest 5 unread notifications
    @GetMapping("/{userId}/unread/latest")
    public ResponseEntity<List<DetailNotificationResponse>> getLatestUnreadNotifications(@PathVariable Long userId) {
        List<DetailNotificationResponse> unreadNotifications = notificationService.findTop5UnreadWithCount(userId);
        return ResponseEntity.ok(unreadNotifications);
    }

    @GetMapping("/{userId}/all")
    public ResponseEntity<Page<NotificationResponse>> getAllNotifications(@PathVariable Long userId,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> allNotifications = notificationService.findByUserId(userId, pageable);
        return ResponseEntity.ok(allNotifications);
    }

    // Mark a notification as read
    @PutMapping("/{notificationId}/mark-as-read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    // Mark all notifications as read
    @PutMapping("/{userId}/mark-all-as-read")
    public ResponseEntity<Void> markAllNotificationsAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}

