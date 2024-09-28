package torquehub.torquehub.business.interfaces;

import org.springframework.stereotype.Service;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionResponse;

import java.util.List;
import java.util.Optional;

@Service
public interface QuestionService {

    QuestionResponse askQuestion(QuestionCreateRequest questionCreateRequest);


    void deleteQuestion(Long questionId);

    boolean updateQuestion(Long questionId, QuestionUpdateRequest questionUpdateRequest);

    Optional<QuestionResponse> getQuestionbyId(Long questionId);

    List<QuestionResponse> getAllQuestions();

    Optional<List<QuestionResponse>> getQuestionsByUser(Long userId);

    List<QuestionResponse> getQuestionsByTag(String tag);

}
