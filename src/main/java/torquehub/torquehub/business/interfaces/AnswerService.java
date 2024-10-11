package torquehub.torquehub.business.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import torquehub.torquehub.domain.request.AnswerDtos.AnswerCreateRequest;
import torquehub.torquehub.domain.request.AnswerDtos.AnswerEditRequest;
import torquehub.torquehub.domain.response.AnswerDtos.AnswerResponse;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;

import java.util.List;
import java.util.Optional;


public interface AnswerService {
    AnswerResponse addAnswer(AnswerCreateRequest answerCreateRequest);
    AnswerResponse editAnswer(Long answerId, AnswerEditRequest answerEditRequest);
    Optional<List<AnswerResponse>> getAnswersByQuestion(Long questionId);
    boolean deleteAnswer(Long answerId);
    AnswerResponse getAnswerById(Long answerId);
    Optional<List<AnswerResponse>> getAnswersByUser(Long userId);
    ReputationResponse upvoteAnswer(Long answerId, Long userId);
    ReputationResponse downvoteAnswer(Long answerId, Long userId);
    ReputationResponse approveBestAnswer(Long questionId, Long answerId, Long userId);
    Page<AnswerResponse> getAnswersByQuestion(Long questionId, Pageable pageable);
    


}
