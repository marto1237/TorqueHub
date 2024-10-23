    package torquehub.torquehub.business.interfaces;

    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
    import torquehub.torquehub.domain.model.jpa_models.JpaUser;
    import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
    import torquehub.torquehub.domain.request.NotificationDtos.CreateNotificationRequest;
    import torquehub.torquehub.domain.response.NotificationDtos.NotificationResponse;
    import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;

    import java.util.List;
    import java.util.Optional;

    public interface NotificationService {
        void notifyAnswerOwner(JpaUser owner, JpaAnswer jpaAnswer, boolean isUpvote, ReputationResponse authorReputation);
        /*NotificationResponse notifyAnswerOwner(CreateNotificationRequest createNotificationRequest);*/
        /*NotificationResponse notifyUserAboutPoints(CreateNotificationRequest createNotificationRequest);*/

        Optional<NotificationResponse> createNotification(CreateNotificationRequest createNotificationRequest);
        Optional<NotificationResponse> notifyUserAboutPoints(JpaUser recipient, int points, String reason, JpaUser voter);
        Optional<NotificationResponse> notifyUserAboutVote(JpaUser questionCreator, String message, JpaUser voter, JpaQuestion question);
        List<NotificationResponse> findTop5ByUserIdUnread(Long userId);
        Page<NotificationResponse> findByUserId(Long userId, Pageable pageable);
        boolean markAsRead(Long notificationId);
        boolean markAllAsRead(Long userId);

    }
