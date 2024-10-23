package torquehub.torquehub.business.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import torquehub.torquehub.business.interfaces.NotificationService;
import torquehub.torquehub.controllers.websocketcontrollers.WebSocketNotificationController;
import torquehub.torquehub.domain.mapper.NotificationMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.model.jpa_models.JpaNotification;
import torquehub.torquehub.domain.request.NotificationDtos.CreateNotificationRequest;
import torquehub.torquehub.domain.response.NotificationDtos.NotificationResponse;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaNotificationRepository;
import torquehub.torquehub.persistence.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final JpaNotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;
    private final WebSocketNotificationController webSocketNotificationController;

    public NotificationServiceImpl(JpaNotificationRepository notificationRepository,
                                   NotificationMapper notificationMapper,
                                   UserRepository userRepository,
                                   WebSocketNotificationController webSocketNotificationController) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.userRepository = userRepository;
        this.webSocketNotificationController = webSocketNotificationController;
    }

    private JpaNotification createJpaNotification(JpaUser recipient, JpaUser voter, String message, int points) {
        return JpaNotification.builder()
                .jpaUser(recipient)
                .voter(voter)
                .message(message)
                .points(points)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
    }

    @Override
    public void notifyAnswerOwner(JpaUser owner, JpaAnswer jpaAnswer, boolean isUpvote, ReputationResponse authorReputation) {
        String voteType = isUpvote ? "upvoted" : "downvoted";
        String message = "Your answer to the question \"" + jpaAnswer.getJpaQuestion().getTitle() + "\" was " + voteType +
                ". Your updated reputation is " + authorReputation.getUpdatedReputationPoints() + ".";

        JpaNotification jpaNotification = JpaNotification.builder()
                .jpaUser(owner)
                .voter(jpaAnswer.getJpaUser())
                .message(message)
                .points(authorReputation.getUpdatedReputationPoints())
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();

        notificationRepository.save(jpaNotification);
    }

    @Override
    public Optional<NotificationResponse> createNotification(CreateNotificationRequest request) {
        JpaUser user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        JpaUser voter = userRepository.findById(request.getVoterId())
                .orElseThrow(() -> new IllegalArgumentException("Voter not found"));

        JpaNotification jpaNotification = createJpaNotification(user, voter, request.getMessage(), request.getPoints());
        JpaNotification savedNotification = notificationRepository.save(jpaNotification);

        return Optional.ofNullable(notificationMapper.toResponse(savedNotification));
    }

    @Override
    public Optional<NotificationResponse> notifyUserAboutPoints(JpaUser recipient, int points, String reason, JpaUser voter) {
        String message = String.format("You have gained %d points for %s.", points, reason);

        JpaNotification jpaNotification = createJpaNotification(recipient, voter, message, points);
        JpaNotification savedNotification = notificationRepository.save(jpaNotification);

        return Optional.ofNullable(notificationMapper.toResponse(savedNotification));
    }

    @Override
    @Transactional
    public Optional<NotificationResponse> notifyUserAboutVote(JpaUser questionCreator, String message, JpaUser voter, JpaQuestion question) {
        if (questionCreator.equals(voter)) {
            // Prevent duplicate notifications if the user is both the voter and the question creator
            return Optional.empty();
        }

        JpaNotification jpaNotification = createJpaNotification(questionCreator, voter, message, voter.getPoints());
        JpaNotification savedNotification = notificationRepository.save(jpaNotification);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // This will be called after the transaction is successfully committed
                NotificationResponse notificationResponse = notificationMapper.toResponse(savedNotification);
                webSocketNotificationController.notifyClients(questionCreator.getId(), notificationResponse);
            }
        });

        return Optional.ofNullable(notificationMapper.toResponse(savedNotification));
    }

    @Override
    public List<NotificationResponse> findTop5ByUserIdUnread(Long userId) {
        return notificationRepository.findByJpaUserIdAndIsReadFalse(userId, Pageable.ofSize(5)) // Limit to top 5
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }


    @Override
    public Page<NotificationResponse> findByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findByJpaUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationMapper::toResponse);
    }


    @Override
    public boolean markAsRead(Long notificationId) {
        JpaNotification notification = notificationRepository.findById(notificationId);
        if (notification == null) {
            return false;
        }
        notification.setRead(true);
        notificationRepository.save(notification);
        return true;
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        List<JpaNotification> notifications = notificationRepository.findByJpaUserIdAndIsReadFalse(userId);
        if (notifications.isEmpty()) {
            return false;
        }
        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
        return true;
    }


    public void notifyClients(Long userId, NotificationResponse notificationResponse) {
        webSocketNotificationController.notifyClients(userId, notificationResponse);
    }
}
