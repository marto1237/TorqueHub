package torquehub.torquehub.controllers.websocketcontrollers;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;

@Controller
public class WebSocketAnswerController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketAnswerController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/questions/{questionId}/answers")
    @SendTo("/topic/answers/{questionId}")
    public JpaAnswer sendAnswer(@DestinationVariable Long questionId, JpaAnswer jpaAnswer) {
        return jpaAnswer;
    }

    public void notifyClients(Long questionId, AnswerResponse answerResponse) {
        messagingTemplate.convertAndSend("/topic/answers/" + questionId, answerResponse);
    }
}
