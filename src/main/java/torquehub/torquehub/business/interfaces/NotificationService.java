package torquehub.torquehub.business.interfaces;

import torquehub.torquehub.domain.model.Answer;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;

public interface NotificationService {
    void notifyAnswerOwner(User owner, Answer answer, boolean isUpvote, ReputationResponse authorReputation);
    /*void notifyFollowers(Question question);*/
    /*void notifyUser(User user, String message);*/
}
