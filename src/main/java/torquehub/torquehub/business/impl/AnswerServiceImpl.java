package torquehub.torquehub.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.interfaces.AnswerService;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.domain.ReputationConstants;
import torquehub.torquehub.domain.mapper.AnswerMapper;
import torquehub.torquehub.domain.mapper.CommentMapper;
import torquehub.torquehub.domain.model.Answer;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.request.AnswerDtos.AnswerCreateRequest;
import torquehub.torquehub.domain.request.AnswerDtos.AnswerEditRequest;
import torquehub.torquehub.domain.request.ReputationDtos.ReputationUpdateRequest;
import torquehub.torquehub.domain.response.AnswerDtos.AnswerResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionResponse;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;
import torquehub.torquehub.persistence.repository.AnswerRepository;
import torquehub.torquehub.persistence.repository.QuestionRepository;
import torquehub.torquehub.persistence.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AnswerServiceImpl implements AnswerService {

    @Autowired
    AnswerMapper answerMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ReputationService reputationService;

    @Override
    @Transactional
    public AnswerResponse addAnswer(AnswerCreateRequest answerCreateRequest) {
        try{
            User user = userRepository.findById(answerCreateRequest.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            Question question = questionRepository.findById(answerCreateRequest.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("Question not found"));

            Answer answer = Answer.builder()
                    .text(answerCreateRequest.getText())
                    .user(user)
                    .question(question)
                    .isEdited(false)
                    .answeredTime(new Date().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                    .votes(0)
                    .build();

            Answer savedAnswer = answerRepository.save(answer);
            ReputationUpdateRequest reputationUpdateRequest = new ReputationUpdateRequest(user.getId(), ReputationConstants.POINTS_NEW_ANSWER);
            ReputationResponse reputationResponse = reputationService.updateReputationForNewAnswer(reputationUpdateRequest);

            AnswerResponse answerResponse = answerMapper.toResponse(savedAnswer, commentMapper);
            answerResponse.setReputationUpdate(reputationResponse);

            return answerResponse;
        }
        catch (Exception e){
            throw new RuntimeException("Error adding answer " + e.getMessage());
        }

    }

    @Override
    @Transactional
    public AnswerResponse editAnswer(Long answerId, AnswerEditRequest answerEditRequest) {
        try {
            Optional<Answer> optionalAnswer = answerRepository.findById(answerId);
            if (optionalAnswer.isPresent()) {
                Answer answer = optionalAnswer.get();
                answer.setText(answerEditRequest.getText());
                answer.setEdited(true);
                Answer savedAnswer = answerRepository.save(answer);

                return answerMapper.toResponse(savedAnswer, commentMapper);
            } else {
                throw new IllegalArgumentException("Answer with ID " + answerId + " not found");
            }
        }catch (Exception e){
            throw new RuntimeException("Error editing answer " + e.getMessage());
        }

    }

    @Override
    public Optional<List<AnswerResponse>> getAnswersByQuestion(Long questionId) {
        List<Answer> answers = answerRepository.findByQuestionId(questionId);
        if (answers.isEmpty()) {
            return Optional.empty();
        }else {
            return Optional.of(answers.stream()
                    .map(answer -> answerMapper.toResponse(answer, commentMapper))
                    .toList());
        }
    }

    @Override
    @Transactional
    public boolean deleteAnswer(Long answerId) {
        try{
            Optional<Answer> optionalAnswer = answerRepository.findById(answerId);
            if (optionalAnswer.isPresent()) {

                User user = optionalAnswer.get().getUser();
                ReputationUpdateRequest reputationUpdateRequest = new ReputationUpdateRequest(user.getId(), ReputationConstants.POINTS_ANSWER_WHEN_DELETED);
                boolean isReputationUpdated = reputationService.updateReputationForAnswerWhenAnswerIsDeleted(reputationUpdateRequest);
                if(!isReputationUpdated) {
                    throw new IllegalArgumentException("Error updating reputation for user with ID " + user.getId());
                }else {
                    answerRepository.deleteById(answerId);
                    return true;
                }
            } else {
                throw new IllegalArgumentException("Answer with ID " + answerId + " not found");
            }
        }catch (Exception e){
            throw new RuntimeException("Error deleting answer: " + e.getMessage());
        }
    }

    @Override
    public AnswerResponse getAnswerById(Long answerId) {
        Optional<Answer> optionalAnswer = answerRepository.findById(answerId);
        if (optionalAnswer.isEmpty()) {
            throw new IllegalArgumentException("Answer not found");
        }

        return answerMapper.toResponse(optionalAnswer.get(), commentMapper);
    }

    @Override
    public Optional<List<AnswerResponse>> getAnswersByUser(Long userId) {
        List<Answer> answers = answerRepository.findByUserId(userId);
        if (answers.isEmpty()) {
            return Optional.empty();
        }else {
            return Optional.of(answers.stream()
                    .map(answer -> answerMapper.toResponse(answer, commentMapper))
                    .toList());
        }
    }


}
