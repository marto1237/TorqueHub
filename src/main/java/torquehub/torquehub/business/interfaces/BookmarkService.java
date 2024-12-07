package torquehub.torquehub.business.interfaces;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkAnswerRequest;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkQuestionRequest;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.bookmark_dtos.BookmarkResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionResponse;

public interface BookmarkService {

    BookmarkResponse bookmarkQuestion(BookmarkQuestionRequest bookmarkRequest);
    BookmarkResponse bookmarkAnswer(BookmarkAnswerRequest bookmarkRequest);
    Page<QuestionResponse> getUserBookmarkedQuestions(Long userId, Pageable pageable);
    Page<AnswerResponse> getUserBookmarkedAnswers(Long userId, Pageable pageable);
}
