package torquehub.torquehub.business.interfaces;


import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkQuestionRequest;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkRequest;
import torquehub.torquehub.domain.response.bookmark_dtos.BookmarkResponse;

import java.util.List;
import java.util.Optional;

public interface BookmarkService {

    BookmarkResponse bookmarkQuestion(BookmarkQuestionRequest bookmarkRequest);
    BookmarkResponse bookmarkAnswer(BookmarkRequest bookmarkRequest);
    Optional<List<BookmarkResponse>> getUserBookmarks(Long userId);
}
