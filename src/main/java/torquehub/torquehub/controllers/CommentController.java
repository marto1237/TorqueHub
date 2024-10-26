package torquehub.torquehub.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.CommentService;
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

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
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
    public ResponseEntity<CommentResponse> addComment(@RequestBody @Validated CommentCreateRequest commentCreateRequest) {
        CommentResponse commentResponse = commentService.addComment(commentCreateRequest);
        return ResponseEntity.ok(commentResponse);
    }

    @PostMapping("/{commentId}/upvote")
    public ResponseEntity<ReputationResponse> upvoteAnswer(@PathVariable Long commentId, @RequestParam Long userId) {
        return ResponseEntity.ok(commentService.upvoteComment(commentId, userId));
    }

    @PostMapping("/{commentId}/downvote")
    public ResponseEntity<ReputationResponse> downvoteAnswer(@PathVariable Long commentId, @RequestParam Long userId) {
        return ResponseEntity.ok(commentService.downvoteComment(commentId, userId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> editComment(@PathVariable Long commentId, @RequestBody @Validated CommentEditRequest commentEditRequest) {
        CommentResponse commentResponse = commentService.editComment(commentId, commentEditRequest);
        return ResponseEntity.ok(commentResponse);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ReputationResponse> deleteComment(@PathVariable Long commentId) {
        boolean isDeleted = commentService.deleteComment(commentId);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
