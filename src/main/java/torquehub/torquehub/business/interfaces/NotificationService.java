    package torquehub.torquehub.business.interfaces;

    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import torquehub.torquehub.domain.model.jpa_models.JpaUser;
    import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
    import torquehub.torquehub.domain.request.notification_dtos.*;
    import torquehub.torquehub.domain.request.vote_dtos.VoteAnswerNotificationRequest;
    import torquehub.torquehub.domain.request.vote_dtos.VoteCommentNotificationRequest;
    import torquehub.torquehub.domain.request.vote_dtos.VoteQuestionNotificationRequest;
    import torquehub.torquehub.domain.response.notification_dtos.NotificationResponse;
    import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;

    import java.util.List;
    import java.util.Optional;

    public interface NotificationService {
        void notifyAnswerOwner(JpaUser owner, JpaAnswer jpaAnswer, boolean isUpvote, ReputationResponse authorReputation);
        Optional<NotificationResponse> notifyAnswerOwnerForNewComment(CreateCommentAnswerRequest commentAnswerRequest);
        Optional<NotificationResponse> createNotification(CreateNotificationRequest createNotificationRequest);
        Optional<NotificationResponse> notifyUserAboutPoints(PointsNotificationRequest pointsRequest);
        Optional<NotificationResponse> notifyUserAboutQuestionVote(VoteQuestionNotificationRequest voteRequest);
        Optional<NotificationResponse> notifyUserAboutAnswerVote(VoteAnswerNotificationRequest voteRequest);
        Optional<NotificationResponse> notifyUserAboutCommentVote(VoteCommentNotificationRequest voteRequest);
        List<NotificationResponse> findTop5ByUserIdUnread(Long userId);
        Page<NotificationResponse> findByUserId(Long userId, Pageable pageable);
        Optional<NotificationResponse> notifyFollowersAboutNewAnswer(NewAnswerNotificationRequest request);
        Optional<NotificationResponse> notifyAnswerFollowersAboutNewComment(NewCommentOnAnswerNotificationRequest request);
        boolean markAsRead(Long notificationId);
        boolean markAllAsRead(Long userId);

    }
