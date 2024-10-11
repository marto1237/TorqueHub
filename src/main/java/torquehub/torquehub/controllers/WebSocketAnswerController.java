package torquehub.torquehub.controllers;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import torquehub.torquehub.domain.model.Answer;
import torquehub.torquehub.domain.response.AnswerDtos.AnswerResponse;

@Controller
public class WebSocketAnswerController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketAnswerController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/questions/{questionId}/answers")
    @SendTo("/topic/answers/{questionId}")
    public Answer sendAnswer(@DestinationVariable Long questionId, Answer answer) {
        return answer;
    }

    public void notifyClients(Long questionId, AnswerResponse answerResponse) {
        System.out.println("Notifying clients for questionId: " + questionId + " with answer: " + answerResponse);
        messagingTemplate.convertAndSend("/topic/answers/" + questionId, answerResponse);
    }
}
