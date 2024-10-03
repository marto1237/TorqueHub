package torquehub.torquehub.business.interfaces;

import org.springframework.stereotype.Service;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionDetailResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionSummaryResponse;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public interface QuestionService {

    QuestionResponse askQuestion(QuestionCreateRequest questionCreateRequest);


    void deleteQuestion(Long questionId);

    boolean updateQuestion(Long questionId, QuestionUpdateRequest questionUpdateRequest);

    Optional<QuestionDetailResponse> getQuestionbyId(Long questionId);

    List<QuestionSummaryResponse> getAllQuestions();

    Optional<List<QuestionSummaryResponse>> getQuestionsByUser(Long userId);

    List<QuestionSummaryResponse> getQuestionsByTags(Set<String> tags);

}
