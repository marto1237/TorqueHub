package torquehub.torquehub.business.interfaces;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkQuestionRequest;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkRequest;
import torquehub.torquehub.domain.response.bookmark_dtos.BookmarkResponse;
public interface BookmarkService {

    BookmarkResponse bookmarkQuestion(BookmarkQuestionRequest bookmarkRequest);
    BookmarkResponse bookmarkAnswer(BookmarkRequest bookmarkRequest);
    Page<BookmarkResponse> getUserBookmarkedQuestions(Long userId, Pageable pageable);
    Page<BookmarkResponse> getUserBookmarkedAnswers(Long userId, Pageable pageable);
}
