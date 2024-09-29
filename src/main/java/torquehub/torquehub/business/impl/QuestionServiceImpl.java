package torquehub.torquehub.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import torquehub.torquehub.business.interfaces.QuestionService;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionResponse;
import torquehub.torquehub.persistence.repository.AnswerRepository;
import torquehub.torquehub.persistence.repository.QuestionRepository;
import torquehub.torquehub.persistence.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;


    @Override
    public QuestionResponse askQuestion(QuestionCreateRequest questionCreateRequest) {
        User user = userRepository.findById(questionCreateRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Question question = Question.builder()
                .title(questionCreateRequest.getTitle())
                .description(questionCreateRequest.getDescription())
                .tags(questionCreateRequest.getTags())
                .user(user)
                .askedTime(LocalDateTime.now())
                .build();

        Question savedQuestion = questionRepository.save(question);

        return convertToResponse(savedQuestion);
    }

    @Override
    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    @Override
    public boolean updateQuestion(Long questionId, QuestionUpdateRequest questionUpdateRequest) {
        Optional<Question> optionalQuestion = questionRepository.findById(questionId);
        if (optionalQuestion.isPresent()) {
            Question existingQuestion = optionalQuestion.get();
            existingQuestion.setTitle(questionUpdateRequest.getTitle());
            existingQuestion.setDescription(questionUpdateRequest.getDescription());
            existingQuestion.setTags(questionUpdateRequest.getTags());
            questionRepository.save(existingQuestion);
            return true;
        }
        return false;

    }

    @Override
    public Optional<QuestionResponse> getQuestionbyId(Long questionId) {
        return  questionRepository.findById(questionId)
                .map(this::convertToResponse);
    }

    @Override
    public List<QuestionResponse> getAllQuestions() {
        return questionRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<List<QuestionResponse>>getQuestionsByUser(Long userId) {
        List<Question> questions = questionRepository.findByUserId(userId);

        if (questions.isEmpty()) {
            return Optional.empty();
        }else {
            return Optional.of(questions.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public List<QuestionResponse> getQuestionsByTag(String tag) {
        List<Question> questions = questionRepository.findByTags(tag);

        if (questions.isEmpty()) {
            throw new IllegalArgumentException("No questions found for this tag.");
        }

        return questions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }



    private QuestionResponse convertToResponse(Question question) {
        return QuestionResponse.builder()
                .id(question.getId())
                .title(question.getTitle())
                .description(question.getDescription())
                .tags(question.getTags())
                .views(question.getViews())
                .votes(question.getVotes())
                .totalAnswers(question.getTotalAnswers())
                .user(question.getUser())
                .askedTime(question.getAskedTime())
                .build();
    }

}
