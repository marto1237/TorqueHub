package torquehub.torquehub.business.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import torquehub.torquehub.business.exeption.answer_exptions.*;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.business.interfaces.VoteService;
import torquehub.torquehub.domain.mapper.AnswerMapper;
import torquehub.torquehub.domain.mapper.CommentMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.request.answer_dtos.AnswerCreateRequest;
import torquehub.torquehub.domain.request.answer_dtos.AnswerEditRequest;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaAnswerRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaQuestionRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaUserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnswerServiceImplTest {

    @Mock private AnswerMapper answerMapper;
    @Mock private CommentMapper commentMapper;
    @Mock private JpaUserRepository userRepository;
    @Mock private JpaQuestionRepository questionRepository;
    @Mock private ReputationService reputationService;
    @Mock private JpaAnswerRepository answerRepository;
    @Mock private VoteService voteService;

    private AnswerServiceImpl answerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        answerService = new AnswerServiceImpl(
                answerMapper,
                commentMapper,
                userRepository,
                questionRepository,
                reputationService,
                answerRepository,
                voteService
        );
    }

    // Add Answer Tests
    @Test
    void addAnswer_Success() {
        // Arrange
        Long userId = 1L;
        Long questionId = 1L;
        JpaUser user = new JpaUser();
        user.setId(userId);

        JpaQuestion question = new JpaQuestion();
        question.setId(questionId);
        question.setTotalAnswers(0);

        AnswerCreateRequest request = new AnswerCreateRequest();
        request.setUserId(userId);
        request.setQuestionId(questionId);
        request.setText("Test answer");

        JpaAnswer savedAnswer = new JpaAnswer();
        AnswerResponse expectedResponse = new AnswerResponse();
        ReputationResponse reputationResponse = new ReputationResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(answerRepository.save(any())).thenReturn(savedAnswer);
        when(answerMapper.toResponse(any(), any())).thenReturn(expectedResponse);
        when(reputationService.updateReputationForNewAnswer(any())).thenReturn(reputationResponse);

        // Act
        AnswerResponse result = answerService.addAnswer(request);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(1, question.getTotalAnswers());
        verify(answerRepository).save(any());
        verify(reputationService).updateReputationForNewAnswer(any());
    }

    @Test
    void addAnswer_UserNotFound() {
        AnswerCreateRequest request = new AnswerCreateRequest();
        request.setUserId(1L);

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(AnswerCreationException.class, () -> answerService.addAnswer(request));
    }

    @Test
    void addAnswer_QuestionNotFound() {
        AnswerCreateRequest request = new AnswerCreateRequest();
        request.setUserId(1L);
        request.setQuestionId(1L);

        when(userRepository.findById(any())).thenReturn(Optional.of(new JpaUser()));
        when(questionRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(AnswerCreationException.class, () -> answerService.addAnswer(request));
    }

    // Edit Answer Tests
    @Test
    void editAnswer_Success() {
        Long answerId = 1L;
        String newText = "Updated answer text";

        JpaAnswer existingAnswer = new JpaAnswer();
        existingAnswer.setId(answerId);
        existingAnswer.setText("Original text");

        AnswerEditRequest request = new AnswerEditRequest();
        request.setText(newText);

        AnswerResponse expectedResponse = new AnswerResponse();

        when(answerRepository.findById(answerId)).thenReturn(Optional.of(existingAnswer));
        when(answerRepository.save(any())).thenReturn(existingAnswer);
        when(answerMapper.toResponse(any(), any())).thenReturn(expectedResponse);

        AnswerResponse result = answerService.editAnswer(answerId, request);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertTrue(existingAnswer.isEdited());
        assertEquals(newText, existingAnswer.getText());
        verify(answerRepository).save(existingAnswer);
    }

    @Test
    void editAnswer_AnswerNotFound() {
        Long answerId = 1L;
        AnswerEditRequest request = new AnswerEditRequest();

        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());

        assertThrows(AnswerEditException.class, () -> answerService.editAnswer(answerId, request));
    }

    // Delete Answer Tests
    @Test
    void deleteAnswer_Success() {
        Long answerId = 1L;
        JpaAnswer answer = new JpaAnswer();
        JpaQuestion question = new JpaQuestion();
        JpaUser user = new JpaUser();
        user.setId(1L);

        question.setTotalAnswers(1);
        answer.setJpaQuestion(question);
        answer.setJpaUser(user);

        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(reputationService.updateReputationForAnswerWhenAnswerIsDeleted(any())).thenReturn(true);

        boolean result = answerService.deleteAnswer(answerId);

        assertTrue(result);
        assertEquals(0, question.getTotalAnswers());
        verify(answerRepository).deleteById(answerId);
    }

    @Test
    void deleteAnswer_WithBestAnswer() {
        Long answerId = 1L;
        JpaAnswer answer = new JpaAnswer();
        JpaQuestion question = new JpaQuestion();
        JpaUser user = new JpaUser();
        user.setId(1L);

        question.setTotalAnswers(1);
        question.setBestAnswerId(answerId);
        answer.setJpaQuestion(question);
        answer.setJpaUser(user);

        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(reputationService.updateReputationForAnswerWhenAnswerIsDeleted(any())).thenReturn(true);
        when(reputationService.updateReputationForBestAnswerIsDeleted(any())).thenReturn(new ReputationResponse());

        boolean result = answerService.deleteAnswer(answerId);

        assertTrue(result);
        assertNull(question.getBestAnswerId());
        verify(reputationService).updateReputationForBestAnswerIsDeleted(any());
    }

    @Test
    void deleteAnswer_AnswerNotFound() {
        Long answerId = 1L;
        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());

        assertThrows(AnswerDeleteException.class, () -> answerService.deleteAnswer(answerId));
    }

    @Test
    void deleteAnswer_ReputationUpdateFailed() {
        Long answerId = 1L;
        JpaAnswer answer = new JpaAnswer();
        JpaQuestion question = new JpaQuestion();
        JpaUser user = new JpaUser();
        user.setId(1L);

        answer.setJpaQuestion(question);
        answer.setJpaUser(user);

        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(reputationService.updateReputationForAnswerWhenAnswerIsDeleted(any())).thenReturn(false);

        assertThrows(AnswerDeleteException.class, () -> answerService.deleteAnswer(answerId));
    }

    // Get Answer Tests
    @Test
    void getAnswerById_Success() {
        Long answerId = 1L;
        JpaAnswer answer = new JpaAnswer();
        AnswerResponse expectedResponse = new AnswerResponse();

        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(answerMapper.toResponse(answer, commentMapper)).thenReturn(expectedResponse);

        AnswerResponse result = answerService.getAnswerById(answerId);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void getAnswerById_NotFound() {
        Long answerId = 1L;
        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> answerService.getAnswerById(answerId));
    }

    // Get Answers By User Tests
    @Test
    void getAnswersByUser_Success() {
        Long userId = 1L;
        List<JpaAnswer> answers = Arrays.asList(new JpaAnswer(), new JpaAnswer());
        AnswerResponse mockResponse = new AnswerResponse();

        when(answerRepository.findByUserId(userId)).thenReturn(answers);
        when(answerMapper.toResponse(any(), any())).thenReturn(mockResponse);

        Optional<List<AnswerResponse>> result = answerService.getAnswersByUser(userId);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
    }

    @Test
    void getAnswersByUser_NoAnswers() {
        Long userId = 1L;
        when(answerRepository.findByUserId(userId)).thenReturn(List.of());

        Optional<List<AnswerResponse>> result = answerService.getAnswersByUser(userId);

        assertTrue(result.isEmpty());
    }

    // Voting Tests
    @Test
    void upvoteAnswer_Success() {
        Long answerId = 1L;
        Long userId = 1L;
        JpaAnswer answer = new JpaAnswer();
        JpaUser user = new JpaUser();
        ReputationResponse expectedResponse = new ReputationResponse();

        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(voteService.handleUpvoteForAnswer(user, answer)).thenReturn(expectedResponse);

        ReputationResponse result = answerService.upvoteAnswer(answerId, userId);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void upvoteAnswer_AnswerNotFound() {
        Long answerId = 1L;
        Long userId = 1L;

        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());

        assertThrows(AnswerUpvoteException.class, () -> answerService.upvoteAnswer(answerId, userId));
    }

    @Test
    void downvoteAnswer_Success() {
        Long answerId = 1L;
        Long userId = 1L;
        JpaAnswer answer = new JpaAnswer();
        JpaUser user = new JpaUser();
        ReputationResponse expectedResponse = new ReputationResponse();

        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(voteService.handleDownvoteForAnswer(user, answer)).thenReturn(expectedResponse);

        ReputationResponse result = answerService.downvoteAnswer(answerId, userId);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void downvoteAnswer_UserNotFound() {
        Long answerId = 1L;
        Long userId = 1L;
        JpaAnswer answer = new JpaAnswer();

        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(AnswerDownvoteException.class, () -> answerService.downvoteAnswer(answerId, userId));
    }

    // Best Answer Tests
    @Test
    void approveBestAnswer_Success() {
        Long questionId = 1L;
        Long answerId = 1L;
        Long userId = 1L;

        JpaQuestion question = new JpaQuestion();
        JpaUser questionOwner = new JpaUser();
        questionOwner.setId(userId);
        question.setJpaUser(questionOwner);

        JpaAnswer answer = new JpaAnswer();
        JpaUser answerOwner = new JpaUser();
        answerOwner.setId(2L);
        answer.setJpaUser(answerOwner);

        ReputationResponse expectedResponse = new ReputationResponse();

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(reputationService.updateReputationForBestAnswer(any())).thenReturn(expectedResponse);

        ReputationResponse result = answerService.approveBestAnswer(questionId, answerId, userId);

        assertNotNull(result);
        assertEquals(answerId, question.getBestAnswerId());
        assertEquals(expectedResponse, result);
    }

    @Test
    void approveBestAnswer_NotQuestionOwner() {
        Long questionId = 1L;
        Long answerId = 1L;
        Long userId = 2L;

        JpaQuestion question = new JpaQuestion();
        JpaUser questionOwner = new JpaUser();
        questionOwner.setId(1L);
        question.setJpaUser(questionOwner);

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        assertThrows(AnswerBestAnswerException.class, () -> answerService.approveBestAnswer(questionId, answerId, userId));
    }
}
