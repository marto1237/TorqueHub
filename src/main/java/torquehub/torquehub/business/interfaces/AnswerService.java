package torquehub.torquehub.business.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import torquehub.torquehub.domain.request.answer_dtos.AnswerCreateRequest;
import torquehub.torquehub.domain.request.answer_dtos.AnswerEditRequest;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;

import java.util.List;
import java.util.Optional;


public interface AnswerService {
    AnswerResponse addAnswer(AnswerCreateRequest answerCreateRequest);
    AnswerResponse editAnswer(Long answerId, AnswerEditRequest answerEditRequest);
    boolean deleteAnswer(Long answerId);
    AnswerResponse getAnswerById(Long answerId);
    Optional<List<AnswerResponse>> getAnswersByUser(Long userId);
    ReputationResponse upvoteAnswer(Long answerId, Long userId);
    ReputationResponse downvoteAnswer(Long answerId, Long userId);
    ReputationResponse approveBestAnswer(Long questionId, Long answerId, Long userId);
    Page<AnswerResponse> getAnswersByQuestion(Long questionId, Pageable pageable);
    boolean isAnswerOwner(Long answerId, String username);




}
