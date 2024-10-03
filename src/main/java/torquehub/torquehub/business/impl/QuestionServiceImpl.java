package torquehub.torquehub.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.interfaces.QuestionService;
import torquehub.torquehub.domain.mapper.AnswerMapper;
import torquehub.torquehub.domain.mapper.QuestionMapper;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.model.Tag;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.response.AnswerDtos.AnswerResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionDetailResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionSummaryResponse;
import torquehub.torquehub.persistence.repository.AnswerRepository;
import torquehub.torquehub.persistence.repository.QuestionRepository;
import torquehub.torquehub.persistence.repository.TagRepository;
import torquehub.torquehub.persistence.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private AnswerMapper answerMapper;

    @Override
    public QuestionResponse askQuestion(QuestionCreateRequest questionCreateRequest) {
        User user = userRepository.findById(questionCreateRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + questionCreateRequest.getUserId() + " not found"));

        Set<Tag> tags = convertTagNamesToTags(questionCreateRequest.getTags());

        Question question = Question.builder()
                .title(questionCreateRequest.getTitle())
                .description(questionCreateRequest.getDescription())
                .tags(tags)
                .user(user)
                .askedTime(LocalDateTime.now())
                .build();

        Question savedQuestion = questionRepository.save(question);
        return questionMapper.toResponse(savedQuestion);
    }

    @Override
    public void deleteQuestion(Long questionId) {
        // Ensure the question exists before deleting
        if (questionRepository.existsById(questionId)) {
            questionRepository.deleteById(questionId);
        } else {
            throw new IllegalArgumentException("Question with ID " + questionId + " not found");
        }
    }

    @Override
    @Transactional
    public boolean updateQuestion(Long questionId, QuestionUpdateRequest questionUpdateRequest) {
        try{
            Optional<Question> optionalQuestion = questionRepository.findById(questionId);
            if (optionalQuestion.isPresent()) {
                Question existingQuestion = optionalQuestion.get();

                // Convert Set<String> to Set<Tag>
                Set<Tag> tags = convertTagNamesToTags(questionUpdateRequest.getTags());

                existingQuestion.setTitle(questionUpdateRequest.getTitle());
                existingQuestion.setDescription(questionUpdateRequest.getDescription());
                existingQuestion.setTags(tags);

                questionRepository.save(existingQuestion);
                return true;
            } else {
                throw new IllegalArgumentException("Question with ID " + questionId + " not found.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error updating question with ID " + questionId);
        }

    }


    @Override
    public Optional<QuestionDetailResponse> getQuestionbyId(Long questionId) {
        return questionRepository.findById(questionId)
                .map(questionMapper::toDetailResponse);
    }

    @Override
    public List<QuestionSummaryResponse> getAllQuestions() {
        return questionRepository.findAll().stream()
                .map(questionMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<List<QuestionSummaryResponse>> getQuestionsByUser(Long userId) {
        List<Question> questions = questionRepository.findByUserId(userId);

        if (questions.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(questions.stream()
                    .map(questionMapper::toSummaryResponse)
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public List<QuestionSummaryResponse> getQuestionsByTags(Set<String> tagsNames) {
        List<Question> filteredQuestions = questionRepository.findQuestionsByTagNames(tagsNames);

        if (filteredQuestions.isEmpty()) {
            throw new IllegalArgumentException("No questions found for these tags: " + tagsNames);
        }
        return filteredQuestions.stream()
                .map(questionMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    private Set<Tag> convertTagNamesToTags(Set<String> tagNames) {
        return tagNames.stream()
                .map(tagName -> tagRepository.findByName(tagName)
                        .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagName)))
                .collect(Collectors.toSet());
    }


}

