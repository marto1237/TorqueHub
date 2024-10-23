package torquehub.torquehub.business.interfaces;


import torquehub.torquehub.domain.request.BookmarkDtos.BookmarkQuestionRequest;
import torquehub.torquehub.domain.request.BookmarkDtos.BookmarkRequest;
import torquehub.torquehub.domain.response.BookmarkDtos.BookmarkResponse;

import java.util.List;
import java.util.Optional;

public interface BookmarkService {

    BookmarkResponse bookmarkQuestion(BookmarkQuestionRequest bookmarkRequest);
    BookmarkResponse bookmarkAnswer(BookmarkRequest bookmarkRequest);
    Optional<List<BookmarkResponse>> getUserBookmarks(Long userId);
}
