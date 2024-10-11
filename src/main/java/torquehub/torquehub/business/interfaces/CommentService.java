package torquehub.torquehub.business.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import torquehub.torquehub.domain.request.CommentDtos.CommentCreateRequest;
import torquehub.torquehub.domain.request.CommentDtos.CommentEditRequest;
import torquehub.torquehub.domain.response.CommentDtos.CommentResponse;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;

import java.util.List;
import java.util.Optional;

public interface CommentService {

    CommentResponse addComment(CommentCreateRequest commentCreateRequest);
    CommentResponse editComment(Long commentId, CommentEditRequest commentEditRequest);
    boolean deleteComment(Long commentId);
    CommentResponse getCommentById(Long commentId);
    Optional<List<CommentResponse>> getCommentsByAnswer(Long answerId);
    Optional<List<CommentResponse>> getCommentsByUser(Long userId);
    ReputationResponse upvoteComment(Long commentId, Long userId);
    ReputationResponse downvoteComment(Long commentId, Long userId);
    Page<CommentResponse> getPaginatedComments(Long answerId, Pageable pageable);


}
