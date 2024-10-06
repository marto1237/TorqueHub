package torquehub.torquehub.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.interfaces.NotificationService;
import torquehub.torquehub.domain.model.Answer;
import torquehub.torquehub.domain.model.Notification;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;
import torquehub.torquehub.persistence.repository.NotificationRepository;

import java.time.LocalDateTime;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void notifyAnswerOwner(User owner, Answer answer, boolean isUpvote, ReputationResponse authorReputation) {
        String voteType = isUpvote ? "upvoted" : "downvoted";
        String message = "Your answer to the question \"" + answer.getQuestion().getTitle() + "\" was " + voteType +
                ". Your updated reputation is " + authorReputation.getUpdatedReputationPoints() + ".";

        Notification notification = Notification.builder()
                .user(owner)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        // Send real-time notification to the user using WebSocket
        messagingTemplate.convertAndSendToUser(
                owner.getUsername(), // Use a unique identifier for the user
                "/queue/notifications", // Destination (private queue)
                notification // The notification message
        );
    }

    @Override
    @Transactional
    public void notifyUserAboutPoints(User user, int points, String reason, User voter) {
        String message = "You have gained " + points + " points for " + reason + ".";

        Notification notification = Notification.builder()
                .user(user)
                .voter(voter)
                .message(message)
                .points(points)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/notifications",
                notification
        );
    }



}
