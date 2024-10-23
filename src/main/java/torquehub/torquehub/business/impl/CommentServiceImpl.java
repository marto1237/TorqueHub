package torquehub.torquehub.business.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.interfaces.CommentService;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.domain.ReputationConstants;
import torquehub.torquehub.domain.mapper.CommentMapper;
import torquehub.torquehub.domain.model.jpa_models.*;
import torquehub.torquehub.domain.request.CommentDtos.CommentCreateRequest;
import torquehub.torquehub.domain.request.CommentDtos.CommentEditRequest;
import torquehub.torquehub.domain.request.ReputationDtos.ReputationUpdateRequest;
import torquehub.torquehub.domain.response.CommentDtos.CommentResponse;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;
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

    public CommentServiceImpl(CommentMapper commentMapper,
                              JpaCommentRepository commentRepository,
                              JpaUserRepository userRepository,
                              JpaAnswerRepository answerRepository,
                              ReputationService reputationService,
                              JpaVoteRepository voteRepository) {
        this.commentMapper = commentMapper;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.answerRepository = answerRepository;
        this.reputationService = reputationService;
        this.voteRepository = voteRepository;
    }

    @Override
    @Transactional
    public CommentResponse addComment(CommentCreateRequest commentCreateRequest) {
        try {
            JpaUser jpaUser = userRepository.findById(commentCreateRequest.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            JpaAnswer jpaAnswer = answerRepository.findById(commentCreateRequest.getAnswerId())
                    .orElseThrow(() -> new IllegalArgumentException("Answer not found"));

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
                throw new IllegalArgumentException("Comment not found");
            }
        }
        catch (IllegalArgumentException e){
            throw new RuntimeException("Error editing comment " + e.getMessage());
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
            throw new RuntimeException("Failed to delete comment: " + e.getMessage());
        }
    }

    @Override
    public CommentResponse getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .map(commentMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
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
    public ReputationResponse upvoteComment(Long commentId, Long userId) {
        try {
            JpaComment jpaComment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("Answer not found"));
            JpaUser jpaUser = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

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

            return reputationService.updateReputationForUpvoteComment(
                    new ReputationUpdateRequest(userId, ReputationConstants.POINTS_UPVOTE_COMMENT)
            );

        }catch (Exception e){
            throw new RuntimeException("Error upvoting comment: " + e.getMessage());
        }
    }

    @Override
    public ReputationResponse downvoteComment(Long commentId, Long userId) {
        try {
            JpaComment jpaComment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("Answer not found"));
            JpaUser jpaUser = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

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

            return reputationService.updateReputationForDownvoteComment(
                    new ReputationUpdateRequest(userId, ReputationConstants.POINTS_DOWNVOTE_COMMENT)
            );

        }catch (Exception e){
            throw new RuntimeException("Error downvoting comment: " + e.getMessage());
        }
    }

    @Override
    public Page<CommentResponse> getPaginatedComments(Long answerId, Pageable pageable) {
        Page<JpaComment> comments = commentRepository.findByAnswerId(answerId, pageable);
        return comments.map(commentMapper::toResponse);
    }

}
