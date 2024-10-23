package torquehub.torquehub.controllers.websocketcontrollers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import torquehub.torquehub.domain.response.NotificationDtos.NotificationResponse;

@Controller
public class WebSocketNotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Use MessageMapping only when clients send a message to this endpoint
    @MessageMapping("/notifications")
    @SendTo("/topic/notifications")  // This broadcasts to all clients subscribed to "/topic/notifications"
    public NotificationResponse broadcastNotification(NotificationResponse notification) {
        // You can process or modify the notification here if needed
        return notification;  // Broadcast the notification to all clients listening to "/topic/notifications"
    }

    // Use this method to notify specific clients based on their user ID
    public void notifyClients(Long userId, NotificationResponse notificationResponse) {
        // Send notification to the specific user's topic
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notificationResponse);
    }
}
