package torquehub.torquehub.controller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import torquehub.torquehub.controllers.websocketcontrollers.WebSocketNotificationController;
import torquehub.torquehub.domain.response.notification_dtos.NotificationResponse;

import static org.mockito.Mockito.verify;

class WebSocketNotificationControllerTest {
    private SimpMessagingTemplate messagingTemplate;
    private WebSocketNotificationController controller;

    @BeforeEach
    void setUp() {
        messagingTemplate = Mockito.mock(SimpMessagingTemplate.class);
        controller = new WebSocketNotificationController(messagingTemplate);
    }
    @Test
    void notifyClients_ShouldSendNotificationToSpecificUser() {
        NotificationResponse notification = new NotificationResponse();
        Long userId = 123L;

        controller.notifyClients(userId, notification);

        verify(messagingTemplate).convertAndSend("/topic/notifications/" + userId, notification);
    }
}

