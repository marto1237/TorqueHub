package torquehub.torquehub.controllers.websocketcontrollers;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.response.comment_dtos.CommentResponse;

@Controller
public class WebSocketCommentController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketCommentController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/answers/{answerId}/comments")
    @SendTo("/topic/answers/{answerId}/comments")
    public JpaAnswer sendAnswer(@DestinationVariable Long questionId, JpaAnswer jpaAnswer) {
        return jpaAnswer;
    }

    public void notifyClients(Long answerId, CommentResponse commentResponse) {
        messagingTemplate.convertAndSend("/topic/answers/" + answerId + "/comments", commentResponse);
    }
}
