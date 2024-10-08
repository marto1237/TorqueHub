package torquehub.torquehub.business.impl;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.interfaces.AnswerService;
import torquehub.torquehub.business.interfaces.NotificationService;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.domain.ReputationConstants;
import torquehub.torquehub.domain.mapper.AnswerMapper;
import torquehub.torquehub.domain.mapper.CommentMapper;
import torquehub.torquehub.domain.model.Answer;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.model.Vote;
import torquehub.torquehub.domain.request.AnswerDtos.AnswerCreateRequest;
import torquehub.torquehub.domain.request.AnswerDtos.AnswerEditRequest;
import torquehub.torquehub.domain.request.ReputationDtos.ReputationUpdateRequest;
import torquehub.torquehub.domain.response.AnswerDtos.AnswerResponse;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;
import torquehub.torquehub.persistence.repository.AnswerRepository;
import torquehub.torquehub.persistence.repository.QuestionRepository;
import torquehub.torquehub.persistence.repository.UserRepository;
import torquehub.torquehub.persistence.repository.VoteRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AnswerServiceImpl implements AnswerService {

    @Autowired
    private AnswerMapper answerMapper;

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

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public AnswerResponse addAnswer(AnswerCreateRequest answerCreateRequest) {
        try{
            User user = userRepository.findById(answerCreateRequest.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            Question question = questionRepository.findById(answerCreateRequest.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("Question not found"));

            // If FetchType.LAZY is required, ensure the objects are fully initialized before using them.
            Hibernate.initialize(user);
            Hibernate.initialize(question);

            Answer answer = Answer.builder()
                    .text(answerCreateRequest.getText())
                    .user(user)
                    .question(question)
                    .isEdited(false)
                    .answeredTime(LocalDateTime.now())
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
            throw new RuntimeException("Error adding answer " + e.getMessage(), e);
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

                Question question = optionalAnswer.get().getQuestion();
                User user = optionalAnswer.get().getUser();
                ReputationUpdateRequest reputationUpdateRequest = new ReputationUpdateRequest(user.getId(), ReputationConstants.POINTS_ANSWER_WHEN_DELETED);

                boolean isReputationUpdated = reputationService.updateReputationForAnswerWhenAnswerIsDeleted(reputationUpdateRequest);
                if(!isReputationUpdated) {
                    throw new IllegalArgumentException("Error updating reputation for user with ID " + user.getId());
                }
                if (question.getBestAnswerId() != null && question.getBestAnswerId().equals(answerId)) {

                    ReputationUpdateRequest bestAnswerReputationUpdateRequest = new ReputationUpdateRequest(user.getId(), ReputationConstants.POINTS_BEST_ANSWER_WHEN_DELETED);
                    reputationService.updateReputationForBestAnswerIsDeleted(bestAnswerReputationUpdateRequest);

                    question.setBestAnswerId(null);
                    questionRepository.save(question);

                }

                answerRepository.deleteById(answerId);
                return true;

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

    @Override
    @Transactional
    public ReputationResponse upvoteAnswer(Long answerId, Long userId) {
        try {
            Answer answer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new IllegalArgumentException("Answer not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Optional<Vote> existingVote = voteRepository.findByUserAndAnswer(user, answer);

            if (existingVote.isPresent()) {
                Vote vote = existingVote.get();

                if (vote.isUpvote()) {
                    voteRepository.delete(vote);
                    answer.setVotes(answer.getVotes() - 1);
                } else {
                    vote.setUpvote(true);
                    voteRepository.save(vote);
                    answer.setVotes(answer.getVotes() + 2);
                }
            } else {
                Vote vote = new Vote();
                vote.setUser(user);
                vote.setAnswer(answer);
                vote.setUpvote(true);
                vote.setVotedAt(LocalDateTime.now());
                voteRepository.save(vote);

                answer.setVotes(answer.getVotes() + 1);
            }

            answerRepository.save(answer);

            ReputationResponse authorReputation = reputationService.updateReputationForUpvote(
                    new ReputationUpdateRequest(answer.getUser().getId(), ReputationConstants.POINTS_UPVOTE_RECEIVED)
            );

            notificationService.notifyAnswerOwner(answer.getUser(), answer, true, authorReputation);

            return reputationService.updateReputationForUpvoteGiven(
                    new ReputationUpdateRequest(userId, ReputationConstants.POINTS_UPVOTE_GIVEN)
            );

        } catch (Exception e) {
            throw new RuntimeException("Error upvoting answer: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReputationResponse downvoteAnswer(Long answerId, Long userId) {
        try {
            Answer answer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new IllegalArgumentException("Answer not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Optional<Vote> existingVote = voteRepository.findByUserAndAnswer(user, answer);

            if (existingVote.isPresent()) {
                Vote vote = existingVote.get();

                if (!vote.isUpvote()) {
                    voteRepository.delete(vote);
                    answer.setVotes(answer.getVotes() + 1);
                } else {
                    vote.setUpvote(false);
                    voteRepository.save(vote);
                    answer.setVotes(answer.getVotes() - 2);
                }
            } else {
                Vote vote = new Vote();
                vote.setUser(user);
                vote.setAnswer(answer);
                vote.setUpvote(false);
                vote.setVotedAt(LocalDateTime.now());
                voteRepository.save(vote);

                answer.setVotes(answer.getVotes() - 1);
            }

            answerRepository.save(answer);

            ReputationResponse authorReputation = reputationService.updateReputationForDownvote(
                    new ReputationUpdateRequest(answer.getUser().getId(), ReputationConstants.POINTS_DOWNVOTE_RECEIVED)
            );

            return reputationService.updateReputationForDownvoteGiven(
                    new ReputationUpdateRequest(userId, ReputationConstants.POINTS_DOWNVOTE_GIVEN)
            );
        } catch (Exception e) {
            throw new RuntimeException("Error downvoting answer: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReputationResponse approveBestAnswer(Long questionId, Long answerId, Long userId) {
        try {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new IllegalArgumentException("Question not found"));

            if (!question.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("Only the question owner can approve the best answer");
            }

            Answer answer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new IllegalArgumentException("Answer not found"));



            question.setBestAnswerId(answerId);
            questionRepository.save(question);

            return reputationService.updateReputationForBestAnswer(
                    new ReputationUpdateRequest(answer.getUser().getId(), ReputationConstants.POINTS_BEST_ANSWER)
            );
        } catch (Exception e) {
            throw new RuntimeException("Error approving best answer: " + e.getMessage());
        }
    }
}
