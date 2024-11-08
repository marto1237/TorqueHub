package torquehub.torquehub.business.impl;

import org.hibernate.Hibernate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.exeption.ErrorMessages;
import torquehub.torquehub.business.exeption.answer_exptions.*;
import torquehub.torquehub.business.exeption.question_exeptions.QuestionNotFoundException;
import torquehub.torquehub.business.exeption.user_exeptions.UserNotFoundException;
import torquehub.torquehub.business.interfaces.AnswerService;
import torquehub.torquehub.business.interfaces.NotificationService;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.business.interfaces.VoteService;
import torquehub.torquehub.domain.ReputationConstants;
import torquehub.torquehub.domain.mapper.AnswerMapper;
import torquehub.torquehub.domain.mapper.CommentMapper;
import torquehub.torquehub.domain.mapper.NotificationMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.model.jpa_models.JpaNotification;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.request.answer_dtos.AnswerCreateRequest;
import torquehub.torquehub.domain.request.answer_dtos.AnswerEditRequest;
import torquehub.torquehub.domain.request.notification_dtos.NewAnswerNotificationRequest;
import torquehub.torquehub.domain.request.reputation_dtos.ReputationUpdateRequest;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;
import torquehub.torquehub.persistence.jpa.impl.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AnswerServiceImpl implements AnswerService {

    private final AnswerMapper answerMapper;
    private final CommentMapper commentMapper;
    private final JpaUserRepository userRepository;
    private final JpaQuestionRepository questionRepository;
    private final ReputationService reputationService;
    private final JpaAnswerRepository answerRepository;
    private final VoteService voteService;
    private final NotificationService notificationService;
    private final JpaNotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final JpaFollowRepository followRepository;
    private final JpaBookmarkRepository bookmarkRepository;


    public AnswerServiceImpl(
            AnswerMapper answerMapper,
            CommentMapper commentMapper,
            JpaUserRepository userRepository,
            JpaQuestionRepository questionRepository,
            ReputationService reputationService,
            JpaAnswerRepository answerRepository,
            VoteService voteService,
            NotificationService notificationService,
            JpaNotificationRepository notificationRepository,
            NotificationMapper notificationMapper,
            JpaFollowRepository followRepository,
            JpaBookmarkRepository bookmarkRepository) {
        this.answerMapper = answerMapper;
        this.commentMapper = commentMapper;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.reputationService = reputationService;
        this.answerRepository = answerRepository;
        this.voteService = voteService;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.followRepository = followRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    private static final String ANSWER_ID_PREFIX = "Answer with ID ";
    private static final String NOT_FOUND_SUFFIX = " not found";

    @Override
    @Transactional
    @CacheEvict(value = "questionDetailsByIdAndUser", key = "#answerCreateRequest.getQuestionId()")
    public AnswerResponse addAnswer(AnswerCreateRequest answerCreateRequest) {
        try{
            JpaUser jpaUser = userRepository.findById(answerCreateRequest.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));
            JpaQuestion jpaQuestion = questionRepository.findById(answerCreateRequest.getQuestionId())
                    .orElseThrow(() -> new QuestionNotFoundException(ErrorMessages.QUESTION_NOT_FOUND));

            // If FetchType.LAZY is required, ensure the objects are fully initialized before using them.
            Hibernate.initialize(jpaUser);
            Hibernate.initialize(jpaQuestion);

            JpaAnswer jpaAnswer = JpaAnswer.builder()
                    .text(answerCreateRequest.getText())
                    .jpaUser(jpaUser)
                    .jpaQuestion(jpaQuestion)
                    .edited(false)
                    .answeredTime(LocalDateTime.now())
                    .votes(0)
                    .build();

            JpaAnswer savedJpaAnswer = answerRepository.save(jpaAnswer);
            jpaQuestion.setTotalAnswers(jpaQuestion.getTotalAnswers() + 1);
            jpaQuestion.setLastActivityTime(LocalDateTime.now());
            ReputationUpdateRequest reputationUpdateRequest = new ReputationUpdateRequest(jpaUser.getId(), ReputationConstants.POINTS_NEW_ANSWER);
            ReputationResponse reputationResponse = reputationService.updateReputationForNewAnswer(reputationUpdateRequest);

            AnswerResponse answerResponse = answerMapper.toResponse(savedJpaAnswer, jpaUser.getId(), bookmarkRepository, followRepository, commentMapper);
            answerResponse.setReputationUpdate(reputationResponse);

            NewAnswerNotificationRequest notificationRequest = new NewAnswerNotificationRequest(
                    jpaQuestion.getId(),
                    savedJpaAnswer.getId(),
                    jpaUser.getId(),
                    "A new answer was posted to a question you follow."
            );
            notificationService.notifyFollowersAboutNewAnswer(notificationRequest);

            JpaNotification notification = notificationMapper.toJpaNotification(notificationRequest, jpaQuestion.getJpaUser(), jpaUser,  jpaUser.getPoints());
            notificationRepository.save(notification);

            return answerResponse;
        }
        catch (Exception e) {
            throw new AnswerCreationException("Error adding answer: " + e.getMessage(), e);
        }

    }

    @Override
    @Transactional
    public AnswerResponse editAnswer(Long answerId, AnswerEditRequest answerEditRequest) {
        try {
            Optional<JpaAnswer> optionalAnswer = answerRepository.findById(answerId);
            if (optionalAnswer.isPresent()) {
                JpaAnswer jpaAnswer = optionalAnswer.get();
                jpaAnswer.setText(answerEditRequest.getText());
                jpaAnswer.setEdited(true);
                JpaAnswer savedJpaAnswer = answerRepository.save(jpaAnswer);

                return answerMapper.toResponse(savedJpaAnswer,  answerEditRequest.getUserId(), bookmarkRepository, followRepository, commentMapper);
            } else {
                throw new IllegalArgumentException(ANSWER_ID_PREFIX + answerId + NOT_FOUND_SUFFIX);
            }
        }catch (Exception e){
            throw new AnswerEditException("Error editing answer: " + e.getMessage(), e);
        }

    }

    @Override
    @Transactional
    @CacheEvict(value = {"answerDetailsById", "answersByQuestion"}, key = "#answerId")
    public boolean deleteAnswer(Long answerId) {
        try{
            Optional<JpaAnswer> optionalAnswer = answerRepository.findById(answerId);
            if (optionalAnswer.isPresent()) {

                JpaQuestion jpaQuestion = optionalAnswer.get().getJpaQuestion();
                JpaUser jpaUser = optionalAnswer.get().getJpaUser();
                ReputationUpdateRequest reputationUpdateRequest = new ReputationUpdateRequest(jpaUser.getId(), ReputationConstants.POINTS_ANSWER_WHEN_DELETED);

                boolean isReputationUpdated = reputationService.updateReputationForAnswerWhenAnswerIsDeleted(reputationUpdateRequest);
                if(!isReputationUpdated) {
                    throw new IllegalArgumentException("Error updating reputation for user with ID " + jpaUser.getId());
                }
                if (jpaQuestion.getBestAnswerId() != null && jpaQuestion.getBestAnswerId().equals(answerId)) {

                    ReputationUpdateRequest bestAnswerReputationUpdateRequest = new ReputationUpdateRequest(jpaUser.getId(), ReputationConstants.POINTS_BEST_ANSWER_WHEN_DELETED);
                    reputationService.updateReputationForBestAnswerIsDeleted(bestAnswerReputationUpdateRequest);

                    jpaQuestion.setBestAnswerId(null);
                    questionRepository.save(jpaQuestion);

                }

                answerRepository.deleteById(answerId);
                jpaQuestion.setTotalAnswers(jpaQuestion.getTotalAnswers() - 1);
                jpaQuestion.setLastActivityTime(LocalDateTime.now());
                return true;

            } else {
                throw new IllegalArgumentException(ANSWER_ID_PREFIX + answerId + NOT_FOUND_SUFFIX);
            }
        }catch (Exception e){
            throw new AnswerDeleteException("Error deleting answer: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "answerDetailsById", key = "#answerId")
    public AnswerResponse getAnswerById(Long answerId) {
        Optional<JpaAnswer> optionalAnswer = answerRepository.findById(answerId);
        if (optionalAnswer.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.ANSWER_NOT_FOUND);
        }

        return answerMapper.toResponse(optionalAnswer.get(), null, bookmarkRepository, followRepository, commentMapper);
    }

    @Override
    @Cacheable(value = "userAnswers", key = "#userId")
    public Optional<List<AnswerResponse>> getAnswersByUser(Long userId) {
        List<JpaAnswer> jpaAnswers = answerRepository.findByUserId(userId);
        if (jpaAnswers.isEmpty()) {
            return Optional.empty();
        }else {
            return Optional.of(jpaAnswers.stream()
                    .map(answer -> answerMapper.toResponse(answer, userId, bookmarkRepository, followRepository, commentMapper))
                    .toList());
        }
    }

    @Override
    @Transactional
    public ReputationResponse upvoteAnswer(Long answerId, Long userId) {
        try {
            JpaAnswer jpaAnswer = findAnswerById(answerId);
            JpaUser jpaUser = findUserById(userId);
            return voteService.handleUpvoteForAnswer(jpaUser, jpaAnswer);
        }
        catch (Exception e) {
            throw new AnswerUpvoteException("Error upvoting answer: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ReputationResponse downvoteAnswer(Long answerId, Long userId) {
        try {
            JpaAnswer jpaAnswer = findAnswerById(answerId);
            JpaUser jpaUser = findUserById(userId);
            return voteService.handleDownvoteForAnswer(jpaUser, jpaAnswer);
        }
        catch (Exception e) {
            throw new AnswerDownvoteException("Error downvoting answer: " + e.getMessage(), e);
        }

    }

    @Override
    @Transactional
    public ReputationResponse approveBestAnswer(Long questionId, Long answerId, Long userId) {
        try {
            JpaQuestion jpaQuestion = questionRepository.findById(questionId)
                    .orElseThrow(() -> new IllegalArgumentException("Question not found"));

            if (!jpaQuestion.getJpaUser().getId().equals(userId)) {
                throw new IllegalArgumentException("Only the question owner can approve the best answer");
            }

            JpaAnswer jpaAnswer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.USER_NOT_FOUND));



            jpaQuestion.setBestAnswerId(answerId);
            questionRepository.save(jpaQuestion);

            return reputationService.updateReputationForBestAnswer(
                    new ReputationUpdateRequest(jpaAnswer.getJpaUser().getId(), ReputationConstants.POINTS_BEST_ANSWER)
            );
        } catch (Exception e) {
            throw new AnswerBestAnswerException("Error approving best answer: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "answersByQuestion", key = "#questionId + '-' + #pageable.pageNumber")
    public Page<AnswerResponse> getAnswersByQuestion(Long questionId, Pageable pageable, Long userId) {
        Page<JpaAnswer> answers = answerRepository.findByQuestionId(questionId, pageable);
        return answers.map(answer -> {
            AnswerResponse answerResponse = answerMapper.toResponse(answer,  userId, bookmarkRepository, followRepository, commentMapper);

            // Set isBookmarked and isFollowing if userId is provided
            if (userId != null) {
                boolean isBookmarked = bookmarkRepository.findByUserIdAndAnswerId(userId, answer.getId()).isPresent();
                boolean isFollowing = followRepository.findByUserIdAndAnswerId(userId, answer.getId()).isPresent();

                answerResponse.setIsBookmarked(isBookmarked);
                answerResponse.setIsFollowing(isFollowing);
            } else {
                answerResponse.setIsBookmarked(false);
                answerResponse.setIsFollowing(false);
            }
            return answerResponse;
        });
    }

    @Override
    public boolean isAnswerOwner(Long answerId, String username) {
        JpaAnswer jpaAnswer = answerRepository.findById(answerId)
                .orElseThrow(() -> new AnswerNotFoundException("Answer not found with ID: " + answerId));

        return jpaAnswer.getJpaUser().getUsername().equals(username);
    }

    @Override
    public Long getQuestionIdByAnswerId(Long answerId) {
        JpaAnswer jpaAnswer = answerRepository.findById(answerId)
                .orElseThrow(() -> new AnswerNotFoundException("Answer not found with ID: " + answerId));
        return jpaAnswer.getJpaQuestion().getId();
    }

    private JpaAnswer findAnswerById(Long answerId) {
        return answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException(ANSWER_ID_PREFIX + answerId + NOT_FOUND_SUFFIX));
    }

    private JpaUser findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + NOT_FOUND_SUFFIX));
    }

}
