package torquehub.torquehub.controllers;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.BookmarkService;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkQuestionRequest;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkRequest;
import torquehub.torquehub.domain.response.bookmark_dtos.BookmarkResponse;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/bookmarks")
@Validated
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final TokenUtil tokenUtil;

    public BookmarkController(BookmarkService bookmarkService,
                              TokenUtil tokenUtil) {
        this.bookmarkService = bookmarkService;
        this.tokenUtil = tokenUtil;
    }

    @PostMapping("/questions/{questionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookmarkResponse> toggleBookmarkQuestion(
            @PathVariable Long questionId,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            BookmarkQuestionRequest bookmarkRequest = new BookmarkQuestionRequest(userId, questionId);
            BookmarkResponse bookmarkResponse = bookmarkService.bookmarkQuestion(bookmarkRequest);
            return ResponseEntity.ok(bookmarkResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/answer")
    public ResponseEntity<BookmarkResponse> bookmarkAnswer(@Valid @RequestBody BookmarkRequest bookmarkRequest) {
        BookmarkResponse response = bookmarkService.bookmarkAnswer(bookmarkRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<BookmarkResponse>> getUserBookmarks(@PathVariable Long userId) {
        Optional<List<BookmarkResponse>> bookmarks = bookmarkService.getUserBookmarks(userId);
        return bookmarks.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
