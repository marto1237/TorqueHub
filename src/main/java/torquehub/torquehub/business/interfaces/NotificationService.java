    package torquehub.torquehub.business.interfaces;

    import torquehub.torquehub.domain.model.Answer;
    import torquehub.torquehub.domain.model.Question;
    import torquehub.torquehub.domain.model.User;
    import torquehub.torquehub.domain.request.NotificationDtos.CreateNotificationRequest;
    import torquehub.torquehub.domain.response.NotificationDtos.NotificationResponse;
    import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;

    public interface NotificationService {
        void notifyAnswerOwner(User owner, Answer answer, boolean isUpvote, ReputationResponse authorReputation);
        void notifyUserAboutPoints(User user, int points, String reason, User voter);
        /*NotificationResponse notifyAnswerOwner(CreateNotificationRequest createNotificationRequest);*/
        /*NotificationResponse notifyUserAboutPoints(CreateNotificationRequest createNotificationRequest);*/

    }
