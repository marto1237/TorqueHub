package torquehub.torquehub.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.CommentService;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.domain.request.comment_dtos.CommentCreateRequest;
import torquehub.torquehub.domain.request.comment_dtos.CommentEditRequest;
import torquehub.torquehub.domain.response.comment_dtos.CommentResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/comments")
@Validated
public class CommentController {

    private final CommentService commentService;
    private final TokenUtil tokenUtil;

    public CommentController(CommentService commentService,
                             TokenUtil tokenUtil) {
        this.commentService = commentService;
        this.tokenUtil = tokenUtil;
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long commentId) {
        CommentResponse commentResponse = commentService.getCommentById(commentId);
        return ResponseEntity.ok(commentResponse);
    }
    @GetMapping("/answer/{answerId}")
    public ResponseEntity<Page<CommentResponse>> getCommentsByAnswer(
            @PathVariable Long answerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CommentResponse> comments = commentService.getPaginatedComments(answerId, pageable);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByUser(@PathVariable Long userId) {
        Optional<List<CommentResponse>> comments = commentService.getCommentsByUser(userId);
        return comments.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/answer/{answerId}/all")
    public ResponseEntity<List<CommentResponse>> getAllCommentsByAnswer(@PathVariable Long answerId) {
        Optional<List<CommentResponse>> comments = commentService.getCommentsByAnswer(answerId);
        return comments.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> addComment(@RequestBody @Validated CommentCreateRequest commentCreateRequest,
                                                      @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            commentCreateRequest.setUserId(userId);
            CommentResponse commentResponse = commentService.addComment(commentCreateRequest);
            return ResponseEntity.ok(commentResponse);
        } catch (InvalidAccessTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }


    @PostMapping("/{commentId}/upvote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReputationResponse> upvoteAnswer(@PathVariable Long commentId, @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            ReputationResponse reputationResponse = commentService.upvoteComment(commentId, userId);
            return ResponseEntity.ok(reputationResponse);
        }  catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    @PostMapping("/{commentId}/downvote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReputationResponse> downvoteAnswer(@PathVariable Long commentId, @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            ReputationResponse reputationResponse = commentService.downvoteComment(commentId, userId);
            return ResponseEntity.ok(reputationResponse);
        }  catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MODERATOR') or @commentService.isCommentOwner(#commentId, authentication.name)")
    public ResponseEntity<CommentResponse> editComment(@PathVariable Long commentId,
                                                       @RequestBody @Validated CommentEditRequest commentEditRequest,
                                                       @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            commentEditRequest.setUserId(userId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        CommentResponse commentResponse = commentService.editComment(commentId, commentEditRequest);
        return ResponseEntity.ok(commentResponse);
    }


    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MODERATOR') or @commentService.isCommentOwner(#commentId, authentication.name)")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, @RequestHeader("Authorization") String token) {

        boolean isDeleted = commentService.deleteComment(commentId);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


}
