package torquehub.torquehub.business.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.exeption.answer_exptions.AnswerNotFoundExeption;
import torquehub.torquehub.business.exeption.comment_exeptions.*;
import torquehub.torquehub.business.exeption.user_exptions.UserNotFoundException;
import torquehub.torquehub.business.interfaces.CommentService;
import torquehub.torquehub.business.interfaces.NotificationService;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.domain.ReputationConstants;
import torquehub.torquehub.domain.mapper.CommentMapper;
import torquehub.torquehub.domain.model.jpa_models.*;
import torquehub.torquehub.domain.request.comment_dtos.CommentCreateRequest;
import torquehub.torquehub.domain.request.comment_dtos.CommentEditRequest;
import torquehub.torquehub.domain.request.notification_dtos.CreateCommentAnswerRequest;
import torquehub.torquehub.domain.request.reputation_dtos.ReputationUpdateRequest;
import torquehub.torquehub.domain.request.vote_dtos.VoteCommentNotificationRequest;
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
    private final JpaVoteRepository voteRepository;
    private final NotificationService notificationService;

    public CommentServiceImpl(CommentMapper commentMapper,
                              JpaCommentRepository commentRepository,
                              JpaUserRepository userRepository,
                              JpaAnswerRepository answerRepository,
                              ReputationService reputationService,
                              JpaVoteRepository voteRepository,
                              NotificationService notificationService) {
        this.commentMapper = commentMapper;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.answerRepository = answerRepository;
        this.reputationService = reputationService;
        this.voteRepository = voteRepository;
        this.notificationService = notificationService;
    }

    private static final String USER_NOT_FOUND = "User not found";
    private static final String ANSWER_NOT_FOUND = "Answer not found";
    private static final String COMMENT_NOT_FOUND = "Comment not found";

    @Override
    @Transactional
    public CommentResponse addComment(CommentCreateRequest commentCreateRequest) {
        try {
            JpaUser jpaUser = userRepository.findById(commentCreateRequest.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
            JpaAnswer jpaAnswer = answerRepository.findById(commentCreateRequest.getAnswerId())
                    .orElseThrow(() -> new AnswerNotFoundExeption(ANSWER_NOT_FOUND));

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

            CommentResponse commentResponse = commentMapper.toResponse(savedJpaComment);
            commentResponse.setReputationResponse(reputationResponse);
            return commentResponse;

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("User or answer not found");
        }
    }

    @Override
    @Transactional
    public CommentResponse editComment(Long commentId, CommentEditRequest commentEditRequest) {
        try {
            Optional<JpaComment> commentOptional = commentRepository.findById(commentId);
            if (commentOptional.isPresent()){
                JpaComment jpaComment = commentOptional.get();
                jpaComment.setText(commentEditRequest.getText());
                jpaComment.setEdited(true);
                JpaComment savedJpaComment = commentRepository.save(jpaComment);

                return commentMapper.toResponse(savedJpaComment);
            } else {
                throw new CommentNotFoundException(COMMENT_NOT_FOUND);
            }
        }
        catch (IllegalArgumentException e){
            throw new CommentEditExeption("Error editing comment " + e.getMessage(), e);
        }
    }

    @Override
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
                throw new IllegalArgumentException("Comment with ID " + commentId + " not found.");
            }
        } catch (Exception e) {
            throw new CommentDeleteExeption("Failed to delete comment: " + e.getMessage(), e);
        }
    }

    @Override
    public CommentResponse getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .map(commentMapper::toResponse)
                .orElseThrow(() -> new CommentNotFoundException(COMMENT_NOT_FOUND));
    }

    @Override
    public Optional<List<CommentResponse>> getCommentsByAnswer(Long answerId) {
        List<JpaComment> jpaComments = commentRepository.findByAnswerId(answerId);
        if (jpaComments.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(jpaComments.stream()
                    .map(commentMapper::toResponse)
                    .toList());
        }
    }

    @Override
    public Optional<List<CommentResponse>> getCommentsByUser(Long userId) {
        List<JpaComment> jpaComments = commentRepository.findByUserId(userId);
        if (jpaComments.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(jpaComments.stream()
                    .map(commentMapper::toResponse)
                    .toList());
        }
    }

    @Override
    @Transactional
    public ReputationResponse upvoteComment(Long commentId, Long userId) {
        try {
            JpaComment jpaComment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new AnswerNotFoundExeption(ANSWER_NOT_FOUND));
            JpaUser jpaUser = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

            Optional<JpaVote> existingVote = voteRepository.findByUserAndJpaComment(jpaUser, jpaComment);

            if (existingVote.isPresent()) {
                JpaVote jpaVote = existingVote.get();

                if (jpaVote.isUpvote()) {
                    voteRepository.delete(jpaVote);
                    jpaComment.setVotes(jpaComment.getVotes() - 1);
                } else {
                    jpaVote.setUpvote(true);
                    voteRepository.save(jpaVote);
                    jpaComment.setVotes(jpaComment.getVotes() + 2);
                }
            } else {
                JpaVote jpaVote = new JpaVote();
                jpaVote.setJpaUser(jpaUser);
                jpaVote.setJpaComment(jpaComment);
                jpaVote.setUpvote(true);
                jpaVote.setVotedAt(LocalDateTime.now());
                voteRepository.save(jpaVote);

                jpaComment.setVotes(jpaComment.getVotes() + 1);
            }

            commentRepository.save(jpaComment);


            ReputationResponse authorReputation = reputationService.updateReputationForUpvote(
                    new ReputationUpdateRequest(jpaComment.getJpaUser().getId(), ReputationConstants.POINTS_UPVOTE_RECEIVED)
            );

            reputationService.updateReputationForUpvoteComment(
                    new ReputationUpdateRequest(userId, ReputationConstants.POINTS_UPVOTE_COMMENT)
            );

            VoteCommentNotificationRequest notificationRequest = new VoteCommentNotificationRequest(
                    jpaComment.getJpaUser().getId(),
                    "User " + jpaUser.getUsername() + " has upvoted your comment: " + jpaComment.getText(),
                    userId,
                    jpaComment.getId()
            );
            notificationService.notifyUserAboutCommentVote(notificationRequest);

            return authorReputation;

        }catch (Exception e){
            throw new CommentUpvoteException("Error upvoting comment: " + e.getMessage(), e);
        }
    }

    @Override
    public ReputationResponse downvoteComment(Long commentId, Long userId) {
        try {
            JpaComment jpaComment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new AnswerNotFoundExeption(ANSWER_NOT_FOUND));
            JpaUser jpaUser = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

            Optional<JpaVote> existingVote = voteRepository.findByUserAndJpaComment(jpaUser, jpaComment);

            if(existingVote.isPresent()){
                JpaVote jpaVote = existingVote.get();

                if(!jpaVote.isUpvote()){
                    voteRepository.delete(jpaVote);
                    jpaComment.setVotes(jpaComment.getVotes() + 1);
                }else{
                    jpaVote.setUpvote(false);
                    voteRepository.save(jpaVote);
                    jpaComment.setVotes(jpaComment.getVotes() - 2);

                }
            }else{
                JpaVote jpaVote = new JpaVote();
                jpaVote.setJpaUser(jpaUser);
                jpaVote.setJpaComment(jpaComment);
                jpaVote.setUpvote(false);
                jpaVote.setVotedAt(LocalDateTime.now());
                voteRepository.save(jpaVote);

                jpaComment.setVotes(jpaComment.getVotes() - 1);
            }
            commentRepository.save(jpaComment);

            ReputationResponse authorReputation = reputationService.updateReputationForDownvote(
                    new ReputationUpdateRequest(jpaComment.getJpaUser().getId(), ReputationConstants.POINTS_DOWNVOTE_RECEIVED)
            );

            reputationService.updateReputationForDownvoteComment(
                    new ReputationUpdateRequest(userId, ReputationConstants.POINTS_DOWNVOTE_COMMENT)
            );

            return authorReputation;

        }catch (Exception e){
            throw new CommentDownvoteException("Error downvoting comment: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<CommentResponse> getPaginatedComments(Long answerId, Pageable pageable) {
        Page<JpaComment> comments = commentRepository.findByAnswerId(answerId, pageable);
        return comments.map(commentMapper::toResponse);
    }

}
