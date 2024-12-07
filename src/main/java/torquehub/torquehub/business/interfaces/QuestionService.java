package torquehub.torquehub.business.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import torquehub.torquehub.domain.request.question_dtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.question_dtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.response.question_dtos.QuestionDetailResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;

import java.util.List;
import java.util.Optional;

public interface QuestionService {

    QuestionResponse askQuestion(QuestionCreateRequest questionCreateRequest);

    boolean deleteQuestion(Long questionId);

    boolean updateQuestion(Long questionId, QuestionUpdateRequest questionUpdateRequest);

    Optional<QuestionDetailResponse> getQuestionbyId(Long questionId, Pageable pageable);
    Optional<QuestionDetailResponse> getQuestionbyId(Long questionId, Pageable pageable, Long userId);

    Page<QuestionSummaryResponse> getAllQuestions(Pageable pageable);

    Optional<List<QuestionSummaryResponse>> getQuestionsByUser(Long userId);
    ReputationResponse upvoteQuestion(Long questionId, Long userId);
    ReputationResponse downvoteQuestion(Long questionId, Long userId);
    boolean incrementQuestionView(Long questionId);
    Long getQuestionCountOfUser(Long userId);

}
