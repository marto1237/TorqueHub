package torquehub.torquehub.business.interfaces;

import torquehub.torquehub.domain.request.AnswerDtos.AddAnswerRequest;
import torquehub.torquehub.domain.response.AnswerDtos.AnswerResponse;

import java.util.List;

public interface AnswerService {
    AnswerResponse addAnswer(AddAnswerRequest addAnswerRequest);
    AnswerResponse editAnswer(Long answerId, String text, AddAnswerRequest addAnswerRequest);
    List<AnswerResponse> getAnswersByQuestion(Long questionId);
}
