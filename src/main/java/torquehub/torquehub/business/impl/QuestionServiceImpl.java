package torquehub.torquehub.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.interfaces.QuestionService;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.domain.ReputationConstants;
import torquehub.torquehub.domain.mapper.AnswerMapper;
import torquehub.torquehub.domain.mapper.QuestionMapper;
import torquehub.torquehub.domain.model.*;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.QuestionDtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.request.ReputationDtos.ReputationUpdateRequest;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionDetailResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionResponse;
import torquehub.torquehub.domain.response.QuestionDtos.QuestionSummaryResponse;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;
import torquehub.torquehub.persistence.repository.*;

import java.time.LocalDateTime;
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
    private ReputationService reputationService;

    @Autowired
    private VoteRepository voteRepository;

    @Override
    public QuestionResponse askQuestion(QuestionCreateRequest questionCreateRequest) {
        try {
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

            ReputationUpdateRequest reputationUpdateRequest = new ReputationUpdateRequest(user.getId(), ReputationConstants.POINTS_NEW_QUESTION);
            ReputationResponse reputationResponse = reputationService.updateReputationForNewQuestion(reputationUpdateRequest);

            QuestionResponse questionResponse = questionMapper.toResponse(savedQuestion);
            questionResponse.setReputationUpdate(reputationResponse);

            return questionResponse;

        } catch (Exception e) {
            throw new RuntimeException("Error asking question"+ e.getMessage());
        }


    }

    @Override
    @Transactional
    public boolean deleteQuestion(Long questionId) {
        try {
            Optional<Question> optionalQuestion = questionRepository.findById(questionId);
            if (optionalQuestion.isPresent()) {
                Question questionToDelete = optionalQuestion.get();

                User user = questionToDelete.getUser();
                ReputationUpdateRequest reputationUpdateRequest = new ReputationUpdateRequest(user.getId(), ReputationConstants.POINTS_QUESTION_WHEN_DELETED);
                boolean isDeleted = reputationService.updateReputationForQuestionWhenQuestionIsDeleted(reputationUpdateRequest);
                if(!isDeleted) {
                    throw new IllegalArgumentException("Error updating reputation for user with ID " + user.getId());
                }
                questionRepository.deleteById(questionId);

                return true;
            } else {
                throw new IllegalArgumentException("Question with ID " + questionId + " not found");

            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete question: " + e.getMessage());
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
    public Page<QuestionSummaryResponse> getAllQuestions(Pageable pageable) {
        Page<Question> questionsPage = questionRepository.findAll(pageable);
        return questionsPage.map(questionMapper::toSummaryResponse);
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
    public Page<QuestionSummaryResponse> getQuestionsByTags(Set<String> tags, Pageable pageable) {
        List<Tag> tagEntities = tags.stream()
                .map(tag -> tagRepository.findByName(tag)
                        .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tag)))
                .collect(Collectors.toList());

        Page<Question> filteredQuestions = questionRepository.findQuestionsByTags(tagEntities, pageable);

        return filteredQuestions.map(questionMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public ReputationResponse upvoteQuestion(Long questionId, Long userId) {
        try {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new IllegalArgumentException("Question with ID " + questionId + " not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));

            Optional<Vote> existingVote = voteRepository.findByUserAndQuestion(user, question);

            if (existingVote.isPresent()) {
                Vote vote = existingVote.get();
                if (vote.isUpvote()) {

                    voteRepository.delete(vote);
                    question.setVotes(question.getVotes() - 1);
                } else {
                    vote.setUpvote(true);
                    voteRepository.save(vote);
                    question.setVotes(question.getVotes() + 2);
                }
            } else {
                Vote vote = new Vote();
                vote.setUser(user);
                vote.setQuestion(question);
                vote.setUpvote(true);
                vote.setVotedAt(LocalDateTime.now());
                voteRepository.save(vote);

                question.setVotes(question.getVotes() + 1);

            }

            questionRepository.save(question);

            ReputationResponse authorReputation = reputationService.updateReputationForUpvote(
                    new ReputationUpdateRequest(question.getUser().getId(), ReputationConstants.POINTS_UPVOTE_RECEIVED));

            return reputationService.updateReputationForUpvoteGiven(
                    new ReputationUpdateRequest(userId, ReputationConstants.POINTS_UPVOTE_GIVEN)
            );



        }catch (Exception e) {
            throw new RuntimeException("Error upvoting question: " + e.getMessage());
        }
    }

    @Override
    public ReputationResponse downvoteQuestion(Long questionId, Long userId) {
        try {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new IllegalArgumentException("Question with ID " + questionId + " not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));

            Optional<Vote> existingVote = voteRepository.findByUserAndQuestion(user, question);

            if (existingVote.isPresent()) {
                Vote vote = existingVote.get();

                if (!vote.isUpvote()) {
                    voteRepository.delete(vote);
                    question.setVotes(question.getVotes() + 1);
                } else {
                    vote.setUpvote(false);
                    voteRepository.save(vote);
                    question.setVotes(question.getVotes() - 2);
                }
            } else {
                Vote vote = new Vote();
                vote.setUser(user);
                vote.setQuestion(question);
                vote.setUpvote(false);
                vote.setVotedAt(LocalDateTime.now());
                voteRepository.save(vote);

                question.setVotes(question.getVotes() - 1);
            }

            questionRepository.save(question);

            ReputationResponse authorReputation = reputationService.updateReputationForUpvote(
                    new ReputationUpdateRequest(question.getUser().getId(), ReputationConstants.POINTS_DOWNVOTE_RECEIVED));

            return reputationService.updateReputationForDownvoteGiven(
                    new ReputationUpdateRequest(userId, ReputationConstants.POINTS_DOWNVOTE_GIVEN)
            );

        }catch (Exception e) {
            throw new RuntimeException("Error downvoting question: " + e.getMessage());
        }
    }

    private Set<Tag> convertTagNamesToTags(Set<String> tagNames) {
        return tagNames.stream()
                .map(tagName -> tagRepository.findByName(tagName)
                        .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagName)))
                .collect(Collectors.toSet());
    }


}

