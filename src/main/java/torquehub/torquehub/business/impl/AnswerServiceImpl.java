package torquehub.torquehub.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import torquehub.torquehub.business.interfaces.AnswerService;
import torquehub.torquehub.domain.model.Answer;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.request.AnswerDtos.AddAnswerRequest;
import torquehub.torquehub.domain.response.AnswerDtos.AnswerResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionResponse;
import torquehub.torquehub.persistence.repository.AnswerRepository;
import torquehub.torquehub.persistence.repository.QuestionRepository;
import torquehub.torquehub.persistence.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AnswerServiceImpl implements AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Override
    public AnswerResponse addAnswer(AddAnswerRequest addAnswerRequest) {
        Optional<User> userOptional = userRepository.findById(addAnswerRequest.getUserId());
        Optional<Question> questionOptional = questionRepository.findById(addAnswerRequest.getQuestionId());
        if (userOptional.isEmpty() || questionOptional.isEmpty()) {
            throw new IllegalArgumentException("User or question not found");
        }

        Answer answer = Answer.builder()
                .text(addAnswerRequest.getText())
                .user(userOptional.get())
                .question(questionOptional.get())
                .isEdited(false)
                .answeredTime(new Date().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                .votes(0)
                .build();

        Answer savedAnswer = answerRepository.save(answer);
        return mapToResponse(savedAnswer);

    }

    @Override
    public AnswerResponse editAnswer(Long answerId, String text, AddAnswerRequest addAnswerRequest) {
        Optional<Answer> optionalAnswer = answerRepository.findById(answerId);
        if (optionalAnswer.isEmpty()) {
            throw new IllegalArgumentException("Answer not found");
        }

        Answer answer = optionalAnswer.get();
        answer.setText(text);
        answer.setEdited(true);
        Answer savedAnswer = answerRepository.save(answer);

        return mapToResponse(savedAnswer);
    }

    @Override
    public List<AnswerResponse> getAnswersByQuestion(Long questionId) {
        List<Answer> answers = answerRepository.findByQuestionId(questionId);
        return answers.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AnswerResponse mapToResponse(Answer answer) {
        return AnswerResponse.builder()
                .id(answer.getId())
                .text(answer.getText())
                .username(answer.getUser().getUsername())
                .votes(answer.getVotes())
                .postedTime(Date.from(answer.getAnsweredTime().atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .build();
    }
}
