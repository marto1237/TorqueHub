package torquehub.torquehub.business.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.interfaces.CommentService;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.domain.ReputationConstants;
import torquehub.torquehub.domain.mapper.CommentMapper;
import torquehub.torquehub.domain.model.*;
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
            User user = userRepository.findById(commentCreateRequest.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            Answer answer = answerRepository.findById(commentCreateRequest.getAnswerId())
                    .orElseThrow(() -> new IllegalArgumentException("Answer not found"));

            Comment comment = Comment.builder()
                    .text(commentCreateRequest.getText())
                    .user(user)
                    .answer(answer)
                    .isEdited(false)
                    .commentedTime(new Date().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                    .votes(0)
                    .build();

            Comment savedComment = commentRepository.save(comment);
            Question question = answer.getQuestion();
            question.setTotalComments(question.getTotalComments() + 1);
            question.setLastActivityTime(LocalDateTime.now());
            ReputationUpdateRequest reputationUpdateRequest = new ReputationUpdateRequest(user.getId(),  ReputationConstants.POINTS_NEW_COMMENT);
            ReputationResponse reputationResponse = reputationService.updateReputationForNewComment(reputationUpdateRequest);

            CommentResponse commentResponse = commentMapper.toResponse(savedComment);
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
            Optional<Comment> commentOptional = commentRepository.findById(commentId);
            if (commentOptional.isPresent()){
                Comment comment = commentOptional.get();
                comment.setText(commentEditRequest.getText());
                comment.setEdited(true);
                Comment savedComment = commentRepository.save(comment);

                return commentMapper.toResponse(savedComment);
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
            Optional<Comment> commentOptional = commentRepository.findById(commentId);
            if (commentOptional.isPresent()) {

                User user = commentOptional.get().getUser();
                ReputationUpdateRequest reputationUpdateRequest = new ReputationUpdateRequest(user.getId(), ReputationConstants.POINTS_COMMENT_WHEN_DELETED);
                boolean isReputationUpdated = reputationService.updateReputationForCommentWhenCommentIsDeleted(reputationUpdateRequest);
                if (!isReputationUpdated) {
                    throw new IllegalArgumentException("Error updating reputation for user with ID " + user.getId());
                }else {
                    commentRepository.deleteById(commentId);
                    Question question = commentOptional.get().getAnswer().getQuestion();
                    question.setTotalComments(question.getTotalComments() - 1);
                    question.setLastActivityTime(LocalDateTime.now());
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
        List<Comment> comments = commentRepository.findByAnswerId(answerId);
        if (comments.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(comments.stream()
                    .map(commentMapper::toResponse)
                    .toList());
        }
    }

    @Override
    public Optional<List<CommentResponse>> getCommentsByUser(Long userId) {
        List<Comment> comments = commentRepository.findByUserId(userId);
        if (comments.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(comments.stream()
                    .map(commentMapper::toResponse)
                    .toList());
        }
    }

    @Override
    public ReputationResponse upvoteComment(Long commentId, Long userId) {
        try {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("Answer not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Optional<Vote> existingVote = voteRepository.findByUserAndComment(user, comment);

            if (existingVote.isPresent()) {
                Vote vote = existingVote.get();

                if (vote.isUpvote()) {
                    voteRepository.delete(vote);
                    comment.setVotes(comment.getVotes() - 1);
                } else {
                    vote.setUpvote(true);
                    voteRepository.save(vote);
                    comment.setVotes(comment.getVotes() + 2);
                }
            } else {
                Vote vote = new Vote();
                vote.setUser(user);
                vote.setComment(comment);
                vote.setUpvote(true);
                vote.setVotedAt(LocalDateTime.now());
                voteRepository.save(vote);

                comment.setVotes(comment.getVotes() + 1);
            }

            commentRepository.save(comment);


            ReputationResponse authorReputation = reputationService.updateReputationForUpvote(
                    new ReputationUpdateRequest(comment.getUser().getId(), ReputationConstants.POINTS_UPVOTE_RECEIVED)
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
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("Answer not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Optional<Vote> existingVote = voteRepository.findByUserAndComment(user, comment);

            if(existingVote.isPresent()){
                Vote vote = existingVote.get();

                if(!vote.isUpvote()){
                    voteRepository.delete(vote);
                    comment.setVotes(comment.getVotes() + 1);
                }else{
                    vote.setUpvote(false);
                    voteRepository.save(vote);
                    comment.setVotes(comment.getVotes() - 2);

                }
            }else{
                Vote vote = new Vote();
                vote.setUser(user);
                vote.setComment(comment);
                vote.setUpvote(false);
                vote.setVotedAt(LocalDateTime.now());
                voteRepository.save(vote);

                comment.setVotes(comment.getVotes() - 1);
            }
            commentRepository.save(comment);

            ReputationResponse authorReputation = reputationService.updateReputationForDownvote(
                    new ReputationUpdateRequest(comment.getUser().getId(), ReputationConstants.POINTS_DOWNVOTE_RECEIVED)
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
        Page<Comment> comments = commentRepository.findByAnswerId(answerId, pageable);
        return comments.map(commentMapper::toResponse);
    }

}
