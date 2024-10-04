package torquehub.torquehub.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.interfaces.CommentService;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.domain.ReputationConstants;
import torquehub.torquehub.domain.mapper.CommentMapper;
import torquehub.torquehub.domain.model.Answer;
import torquehub.torquehub.domain.model.Comment;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.request.CommentDtos.CommentCreateRequest;
import torquehub.torquehub.domain.request.CommentDtos.CommentEditRequest;
import torquehub.torquehub.domain.request.ReputationDtos.ReputationUpdateRequest;
import torquehub.torquehub.domain.response.CommentDtos.CommentResponse;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;
import torquehub.torquehub.persistence.repository.AnswerRepository;
import torquehub.torquehub.persistence.repository.CommentRepository;
import torquehub.torquehub.persistence.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private ReputationService reputationService;


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

}
