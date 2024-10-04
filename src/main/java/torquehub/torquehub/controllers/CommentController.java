package torquehub.torquehub.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.CommentService;
import torquehub.torquehub.domain.request.CommentDtos.CommentCreateRequest;
import torquehub.torquehub.domain.request.CommentDtos.CommentEditRequest;
import torquehub.torquehub.domain.response.CommentDtos.CommentResponse;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/comments")
@Validated
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long commentId) {
        CommentResponse commentResponse = commentService.getCommentById(commentId);
        return ResponseEntity.ok(commentResponse);
    }
    @GetMapping("/answer/{answerId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByAnswer(@PathVariable Long answerId) {
        Optional<List<CommentResponse>> comments = commentService.getCommentsByAnswer(answerId);
        return comments.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByUser(@PathVariable Long userId) {
        Optional<List<CommentResponse>> comments = commentService.getCommentsByUser(userId);
        return comments.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(@RequestBody @Validated CommentCreateRequest commentCreateRequest) {
        CommentResponse commentResponse = commentService.addComment(commentCreateRequest);
        return ResponseEntity.ok(commentResponse);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> editComment(@PathVariable Long commentId, @RequestBody @Validated CommentEditRequest commentEditRequest) {
        CommentResponse commentResponse = commentService.editComment(commentId, commentEditRequest);
        return ResponseEntity.ok(commentResponse);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Boolean> deleteComment(@PathVariable Long commentId) {
        boolean isDeleted = commentService.deleteComment(commentId);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
