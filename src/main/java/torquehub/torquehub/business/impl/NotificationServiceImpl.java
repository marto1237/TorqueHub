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
import torquehub.torquehub.domain.request.notification_dtos.CreateCommentAnswerRequest;
import torquehub.torquehub.domain.request.notification_dtos.CreateNotificationRequest;
import torquehub.torquehub.domain.request.notification_dtos.PointsNotificationRequest;
import torquehub.torquehub.domain.request.vote_dtos.VoteAnswerNotificationRequest;
import torquehub.torquehub.domain.request.vote_dtos.VoteCommentNotificationRequest;
import torquehub.torquehub.domain.request.vote_dtos.VoteQuestionNotificationRequest;
import torquehub.torquehub.domain.response.notification_dtos.NotificationResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaNotificationRepository;
import torquehub.torquehub.persistence.repository.QuestionRepository;
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
    private final QuestionRepository questionRepository;

    public NotificationServiceImpl(JpaNotificationRepository notificationRepository,
                                   NotificationMapper notificationMapper,
                                   UserRepository userRepository,
                                   WebSocketNotificationController webSocketNotificationController,
                                   QuestionRepository questionRepository) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.userRepository = userRepository;
        this.webSocketNotificationController = webSocketNotificationController;
        this.questionRepository = questionRepository;
    }

    private static final String USER_NOT_FOUND = "User not found";
    private static final String VOTER_NOT_FOUND = "Voter not found";
    private static final String QUESTION_NOT_FOUND = "Question not found";
    private static final String ANSWER_OWNER_NOT_FOUND = "Answer owner not found";
    private static final String COMMENTER_NOT_FOUND = "Commenter not found";

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
    @Transactional
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
    @Transactional
    public Optional<NotificationResponse> createNotification(CreateNotificationRequest request) {
        JpaUser user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
        JpaUser voter = userRepository.findById(request.getVoterId())
                .orElseThrow(() -> new IllegalArgumentException(VOTER_NOT_FOUND));

        JpaNotification jpaNotification = createJpaNotification(user, voter, request.getMessage(), request.getPoints());
        JpaNotification savedNotification = notificationRepository.save(jpaNotification);

        return Optional.ofNullable(notificationMapper.toResponse(savedNotification));
    }

    @Override
    public Optional<NotificationResponse> notifyUserAboutPoints(PointsNotificationRequest pointsRequest) {

        JpaUser recipient = userRepository.findById(pointsRequest.getRecipient().getId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));
        JpaUser voter = userRepository.findById(pointsRequest.getVoter().getId())
                .orElseThrow(() -> new IllegalArgumentException(VOTER_NOT_FOUND));

        String message = String.format("You have gained %d points for %s.", pointsRequest.getPoints(), pointsRequest.getReason());

        JpaNotification jpaNotification = createJpaNotification(recipient, voter, message, pointsRequest.getPoints());
        JpaNotification savedNotification = notificationRepository.save(jpaNotification);

        return Optional.ofNullable(notificationMapper.toResponse(savedNotification));
    }

    @Override
    @Transactional
    public Optional<NotificationResponse> notifyUserAboutQuestionVote(VoteQuestionNotificationRequest request) {
        JpaUser questionCreator = userRepository.findById(request.getQuestionCreatorId())
                .orElseThrow(() -> new IllegalArgumentException("Question creator not found"));
        JpaUser voter = userRepository.findById(request.getVoterId())
                .orElseThrow(() -> new IllegalArgumentException(VOTER_NOT_FOUND));
        JpaQuestion question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException(QUESTION_NOT_FOUND));

        // Avoid sending notification to the same person
        if (questionCreator.equals(voter)|| question.getJpaUser().equals(voter)) {
            return Optional.empty();
        }


        JpaNotification jpaNotification = createJpaNotification(questionCreator, voter, request.getMessage(), voter.getPoints());
        JpaNotification savedNotification = notificationRepository.save(jpaNotification);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                NotificationResponse notificationResponse = notificationMapper.toResponse(savedNotification);
                notifyClients(questionCreator.getId(), notificationResponse);
            }
        });

        return Optional.ofNullable(notificationMapper.toResponse(savedNotification));

    }

    @Override
    public Optional<NotificationResponse> notifyUserAboutAnswerVote(VoteAnswerNotificationRequest request) {
        JpaUser answerCreator = userRepository.findById(request.getAnswerCreatorId())
                .orElseThrow(() -> new IllegalArgumentException("Answer creator not found"));
        JpaUser voter = userRepository.findById(request.getVoterId())
                .orElseThrow(() -> new IllegalArgumentException(VOTER_NOT_FOUND));
        JpaQuestion question = questionRepository.findById(request.getAnswerId())
                .orElseThrow(() -> new IllegalArgumentException(QUESTION_NOT_FOUND));

        // Avoid sending notification to the same person
        if (answerCreator.equals(voter)|| question.getJpaUser().equals(voter)) {
            return Optional.empty();
        }


        JpaNotification jpaNotification = createJpaNotification(answerCreator, voter, request.getMessage(), voter.getPoints());
        JpaNotification savedNotification = notificationRepository.save(jpaNotification);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                NotificationResponse notificationResponse = notificationMapper.toResponse(savedNotification);
                notifyClients(answerCreator.getId(), notificationResponse);
            }
        });

        return Optional.ofNullable(notificationMapper.toResponse(savedNotification));
    }


    @Override
    public Optional<NotificationResponse> notifyAnswerOwnerForNewComment(CreateCommentAnswerRequest createCommentAnswerRequest) {
        JpaUser answerOwner = userRepository.findById(createCommentAnswerRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(ANSWER_OWNER_NOT_FOUND));
        JpaUser commenter = userRepository.findById(createCommentAnswerRequest.getVoterId())
                .orElseThrow(() -> new IllegalArgumentException(COMMENTER_NOT_FOUND));

        // Create the notification for the answer owner
        JpaNotification jpaNotification = JpaNotification.builder()
                .jpaUser(answerOwner)
                .voter(commenter)
                .message(createCommentAnswerRequest.getMessage())
                .points(createCommentAnswerRequest.getPoints())
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();

        JpaNotification savedNotification = notificationRepository.save(jpaNotification);

        // Trigger real-time notification through WebSocket after transaction commit
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                NotificationResponse notificationResponse = notificationMapper.toResponse(savedNotification);
                notifyClients(answerOwner.getId(), notificationResponse);
            }
        });

        return Optional.ofNullable(notificationMapper.toResponse(savedNotification));
    }

    @Override
    public Optional<NotificationResponse> notifyUserAboutCommentVote(VoteCommentNotificationRequest request) {
        JpaUser commentOwner = userRepository.findById(request.getCommentOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("Comment owner not found"));
        JpaUser voter = userRepository.findById(request.getVoterId())
                .orElseThrow(() -> new IllegalArgumentException(VOTER_NOT_FOUND));

        JpaNotification jpaNotification = JpaNotification.builder()
                .jpaUser(commentOwner)
                .voter(voter)
                .message(request.getMessage())
                .points(voter.getPoints())
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();

        JpaNotification savedNotification = notificationRepository.save(jpaNotification);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                NotificationResponse notificationResponse = notificationMapper.toResponse(savedNotification);
                notifyClients(commentOwner.getId(), notificationResponse);
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
