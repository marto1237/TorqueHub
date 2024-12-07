package torquehub.torquehub.controllers;


import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.BookmarkService;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkAnswerRequest;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkQuestionRequest;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.bookmark_dtos.BookmarkResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionResponse;

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

    @PostMapping("/question/{questionId}")
    @PreAuthorize("isAuthenticated()")
    @CacheEvict(value = {"questionDetailsByIdAndUser", "userBookmarkedQuestions"}, key = "#questionId", allEntries = true)
    public ResponseEntity<BookmarkResponse> toggleBookmarkQuestion(
            @PathVariable Long questionId,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            BookmarkQuestionRequest bookmarkRequest = new BookmarkQuestionRequest(userId, questionId);
            BookmarkResponse bookmarkResponse = bookmarkService.bookmarkQuestion(bookmarkRequest);
            return ResponseEntity.ok(bookmarkResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/answer/{answerId}")
    @PreAuthorize("isAuthenticated()")
    @CacheEvict(value = {"questionDetailsByIdAndUser", "userBookmarkedAnswers"}, key = "#answerId", allEntries = true)
    public ResponseEntity<BookmarkResponse> toggleBookmarkAnswer(
            @PathVariable Long answerId,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            BookmarkAnswerRequest bookmarkRequest = new BookmarkAnswerRequest(userId, answerId);
            BookmarkResponse bookmarkResponse = bookmarkService.bookmarkAnswer(bookmarkRequest);
            return ResponseEntity.ok(bookmarkResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/answer")
    @PreAuthorize("isAuthenticated()")
    @CacheEvict(value = {"questionDetailsByIdAndUser", "userBookmarkedAnswers"}, allEntries = true)
    public ResponseEntity<BookmarkResponse> bookmarkAnswer(@Valid @RequestBody BookmarkAnswerRequest bookmarkRequest,
                                                           @RequestHeader("Authorization") String token) {
        try {
            Long userId = tokenUtil.getUserIdFromToken(token);
            bookmarkRequest.setUserId(userId); // Set the user ID from the token to the request object
            BookmarkResponse response = bookmarkService.bookmarkAnswer(bookmarkRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/questions/{userId}")
    public ResponseEntity<Page<QuestionResponse>> getUserBookmarkedQuestions(
            @PathVariable Long userId,
            Pageable pageable) {
        try {

            Page<QuestionResponse> bookmarks = bookmarkService.getUserBookmarkedQuestions(userId, pageable);
            return ResponseEntity.ok(bookmarks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/answers/{userId}")
    public ResponseEntity<Page<AnswerResponse>> getUserBookmarkedAnswers(
            @PathVariable Long userId,
            Pageable pageable) {
        try {
            Page<AnswerResponse> bookmarks = bookmarkService.getUserBookmarkedAnswers(userId, pageable);
            return ResponseEntity.ok(bookmarks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
