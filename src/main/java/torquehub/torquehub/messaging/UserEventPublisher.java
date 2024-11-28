package torquehub.torquehub.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(UserEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public UserEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserValidationMessage(Long userId, boolean isValid) {
        try {
            UserValidationMessage message = new UserValidationMessage(userId, isValid);
            rabbitTemplate.convertAndSend("app.exchange", "user.validation", message);
            logger.info("Published message to RabbitMQ: {}", message);
        } catch (Exception e) {
            logger.error("Failed to publish message: {}", e.getMessage(), e);
        }
    }
}

