package torquehub.torquehub.business.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.exeption.answer_exptions.AnswerNotFoundException;
import torquehub.torquehub.business.exeption.comment_exeptions.*;
import torquehub.torquehub.business.exeption.user_exeptions.UserNotFoundException;
import torquehub.torquehub.business.interfaces.CommentService;
import torquehub.torquehub.business.interfaces.NotificationService;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.business.interfaces.VoteService;
import torquehub.torquehub.domain.ReputationConstants;
import torquehub.torquehub.domain.mapper.CommentMapper;
import torquehub.torquehub.domain.model.jpa_models.*;
import torquehub.torquehub.domain.request.comment_dtos.CommentCreateRequest;
import torquehub.torquehub.domain.request.comment_dtos.CommentEditRequest;
import torquehub.torquehub.domain.request.notification_dtos.CreateCommentAnswerRequest;
import torquehub.torquehub.domain.request.notification_dtos.NewAnswerNotificationRequest;
import torquehub.torquehub.domain.request.reputation_dtos.ReputationUpdateRequest;
import torquehub.torquehub.domain.response.comment_dtos.CommentResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaAnswerRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaCommentRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaUserRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaVoteRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final JpaCommentRepository commentRepository;
    private final JpaUserRepository userRepository;
    private final JpaAnswerRepository answerRepository;
    private final ReputationService reputationService;
    private final NotificationService notificationService;
    private final VoteService voteService;
    private final JpaVoteRepository voteRepository;

    public CommentServiceImpl(CommentMapper commentMapper,
                              JpaCommentRepository commentRepository,
                              JpaUserRepository userRepository,
                              JpaAnswerRepository answerRepository,
                              ReputationService reputationService,
                              NotificationService notificationService,
                              VoteService voteService,
                              JpaVoteRepository voteRepository) {
        this.commentMapper = commentMapper;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.answerRepository = answerRepository;
        this.reputationService = reputationService;
        this.notificationService = notificationService;
        this.voteService = voteService;
        this.voteRepository = voteRepository;
    }

    private static final String USER_NOT_FOUND = "User not found";
    private static final String ANSWER_NOT_FOUND = "Answer not found";
    private static final String COMMENT_NOT_FOUND = "Comment not found";
    private static final String COMMENT_ID_PREFIX = "Comment with ID ";
    private static final String NOT_FOUND_SUFFIX = " not found";

    @Override
    @Transactional
    @CacheEvict(value = "questionDetailsByIdAndUser", key = "#commentCreateRequest.getAnswerId()")
    public CommentResponse addComment(CommentCreateRequest commentCreateRequest) {
        try {
            JpaUser jpaUser = userRepository.findById(commentCreateRequest.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
            JpaAnswer jpaAnswer = answerRepository.findById(commentCreateRequest.getAnswerId())
                    .orElseThrow(() -> new AnswerNotFoundException(ANSWER_NOT_FOUND));

            JpaComment jpaComment = JpaComment.builder()
                    .text(commentCreateRequest.getText())
                    .jpaUser(jpaUser)
                    .jpaAnswer(jpaAnswer)
                    .isEdited(false)
                    .commentedTime(new Date().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                    .votes(0)
                    .build();

            JpaComment savedJpaComment = commentRepository.save(jpaComment);
            JpaQuestion jpaQuestion = jpaAnswer.getJpaQuestion();
            jpaQuestion.setTotalComments(jpaQuestion.getTotalComments() + 1);
            jpaQuestion.setLastActivityTime(LocalDateTime.now());
            ReputationUpdateRequest reputationUpdateRequest = new ReputationUpdateRequest(jpaUser.getId(),  ReputationConstants.POINTS_NEW_COMMENT);
            ReputationResponse reputationResponse = reputationService.updateReputationForNewComment(reputationUpdateRequest);

            // Notify the answer owner about the new comment
            CreateCommentAnswerRequest notificationRequest = CreateCommentAnswerRequest.builder()
                    .userId(jpaAnswer.getJpaUser().getId()) // The answer owner
                    .voterId(jpaUser.getId()) // The user who commented
                    .points(jpaUser.getPoints())
                    .message("Someone commented on your answer to the question: \"" + jpaQuestion.getTitle() + "\".")
                    .build();
            notificationService.notifyAnswerOwnerForNewComment(notificationRequest);

            notificationService.notifyFollowersAboutNewAnswer(
                    new NewAnswerNotificationRequest(
                            jpaQuestion.getId(),
                            savedJpaComment.getId(),
                            jpaUser.getId(),
                            "A new comment was posted to a answer you follow."
                    )
            );

            CommentResponse commentResponse = commentMapper.toResponse(savedJpaComment, jpaUser.getId(), voteRepository);
            commentResponse.setReputationResponse(reputationResponse);
            return commentResponse;

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("User or answer not found");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "questionDetailsByIdAndUser", key = "#commentId")
    public CommentResponse editComment(Long commentId, CommentEditRequest commentEditRequest) {
        try {
            Optional<JpaComment> commentOptional = commentRepository.findById(commentId);
            if (commentOptional.isPresent()){
                JpaComment jpaComment = commentOptional.get();
                jpaComment.setText(commentEditRequest.getText());
                jpaComment.setEdited(true);
                JpaComment savedJpaComment = commentRepository.save(jpaComment);

                return commentMapper.toResponse(savedJpaComment, null, voteRepository);
            } else {
                throw new CommentNotFoundException(COMMENT_NOT_FOUND);
            }
        }
        catch (IllegalArgumentException e){
            throw new CommentEditExeption("Error editing comment " + e.getMessage(), e);
        }
    }

    @Override
    @CacheEvict(value = {"commentsByAnswer", "commentsByUser"}, allEntries = true)
    @Transactional
    public boolean deleteComment(Long commentId) {
        try {
            Optional<JpaComment> commentOptional = commentRepository.findById(commentId);
            if (commentOptional.isPresent()) {

                JpaUser jpaUser = commentOptional.get().getJpaUser();
                ReputationUpdateRequest reputationUpdateRequest = new ReputationUpdateRequest(jpaUser.getId(), ReputationConstants.POINTS_COMMENT_WHEN_DELETED);
                boolean isReputationUpdated = reputationService.updateReputationForCommentWhenCommentIsDeleted(reputationUpdateRequest);
                if (!isReputationUpdated) {
                    throw new IllegalArgumentException("Error updating reputation for user with ID " + jpaUser.getId());
                }else {
                    commentRepository.deleteById(commentId);
                    JpaQuestion jpaQuestion = commentOptional.get().getJpaAnswer().getJpaQuestion();
                    jpaQuestion.setTotalComments(jpaQuestion.getTotalComments() - 1);
                    jpaQuestion.setLastActivityTime(LocalDateTime.now());
                    return true;
                }
            } else {
                throw new IllegalArgumentException(COMMENT_ID_PREFIX + commentId + " not found.");
            }
        } catch (Exception e) {
            throw new CommentDeleteExeption("Failed to delete comment: " + e.getMessage(), e);
        }
    }

    @Override
    public CommentResponse getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .map(comment -> commentMapper.toResponse(comment, null, voteRepository))
                .orElseThrow(() -> new CommentNotFoundException(COMMENT_NOT_FOUND));
    }

    @Override
    public Optional<List<CommentResponse>> getCommentsByAnswer(Long answerId) {
        List<JpaComment> jpaComments = commentRepository.findByAnswerId(answerId);
        if (jpaComments.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(jpaComments.stream()
                    .map(comment -> commentMapper.toResponse(comment, null, voteRepository))
                    .toList());
        }
    }

    @Override
    @Cacheable(value = "commentsByUser", key = "#userId")
    public Optional<List<CommentResponse>> getCommentsByUser(Long userId) {
        List<JpaComment> jpaComments = commentRepository.findByUserId(userId);
        if (jpaComments.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(jpaComments.stream()
                    .map(comment -> commentMapper.toResponse(comment, userId, voteRepository))
                    .toList());
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "questionDetailsByIdAndUser", key = "#root.target.getQuestionIdByCommentId(#commentId)")
    public ReputationResponse upvoteComment(Long commentId, Long userId) {
        try {
            JpaComment jpaComment = findCommentById(commentId);
            JpaUser jpaUser = findUserById(userId);
            return voteService.handleUpvoteForComment(jpaUser, jpaComment);
        }
        catch (Exception e) {
            throw new CommentUpvoteException("Error upvoting answer: " + e.getMessage(), e);
        }
    }

    @Override
    @CacheEvict(value = "questionDetailsByIdAndUser", key = "#root.target.getQuestionIdByCommentId(#commentId)")
    public ReputationResponse downvoteComment(Long commentId, Long userId) {
        try {
            JpaComment jpaComment = findCommentById(commentId);
            JpaUser jpaUser = findUserById(userId);
            return voteService.handleDownvoteForComment(jpaUser, jpaComment);
        }
        catch (Exception e) {
            throw new CommentDownvoteException("Error downvote answer: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "commentsByAnswer", key = "#answerId + '-' + #pageable.pageNumber")
    public Page<CommentResponse> getPaginatedComments(Long answerId, Pageable pageable) {
        Page<JpaComment> comments = commentRepository.findByAnswerId(answerId, pageable);
        return comments.map(comment -> commentMapper.toResponse(comment, null, voteRepository));
    }

    public Long getQuestionIdByCommentId(Long commentId) {
        JpaComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with ID: " + commentId));
        return comment.getJpaAnswer().getJpaQuestion().getId();
    }

    private JpaComment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException(COMMENT_ID_PREFIX + commentId + NOT_FOUND_SUFFIX));
    }

    private JpaUser findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + NOT_FOUND_SUFFIX));
    }

    public boolean isCommentOwner(Long commentId, String username) {
        JpaComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(COMMENT_NOT_FOUND));
        return comment.getJpaUser().getUsername().equals(username);
    }
}
