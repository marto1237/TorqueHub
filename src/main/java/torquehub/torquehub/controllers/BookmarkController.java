package torquehub.torquehub.controllers;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.BookmarkService;
import torquehub.torquehub.domain.request.BookmarkDtos.BookmarkRequest;
import torquehub.torquehub.domain.response.BookmarkDtos.BookmarkResponse;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/bookmarks")
@Validated
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    @PostMapping("/question")
    public ResponseEntity<BookmarkResponse> bookmarkQuestion(@Valid @RequestBody BookmarkRequest bookmarkRequest) {
        BookmarkResponse response = bookmarkService.bookmarkQuestion(bookmarkRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
