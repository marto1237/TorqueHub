package torquehub.torquehub.controller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import torquehub.torquehub.business.interfaces.QuestionService;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.controllers.QuestionController;
import torquehub.torquehub.domain.request.question_dtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.question_dtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionDetailResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionControllerTest {

    @InjectMocks
    private QuestionController questionController;

    @Mock
    private QuestionService questionService;

    @Mock
    private TokenUtil tokenUtil;

    private QuestionCreateRequest createRequest;
    private QuestionUpdateRequest updateRequest;
    private QuestionDetailResponse detailResponse;
    private QuestionResponse questionResponse;
    private String testToken;

    @BeforeEach
    void setUp() {
        createRequest = new QuestionCreateRequest();
        createRequest.setTitle("Test Question");
        createRequest.setDescription("Test Description");
        createRequest.setTags(Collections.singleton("test-tag"));

        updateRequest = new QuestionUpdateRequest();
        updateRequest.setTitle("Updated Question");
        updateRequest.setDescription("Updated Description");
        updateRequest.setTags(Collections.singleton("updated-tag"));

        detailResponse = new QuestionDetailResponse();
        questionResponse = new QuestionResponse();
        testToken = "Bearer test-token";
    }

    @Test
    void shouldGetAllQuestionsSuccessfully() {
        Page<QuestionSummaryResponse> questions = new PageImpl<>(Collections.singletonList(new QuestionSummaryResponse()));
        when(questionService.getAllQuestions(any(PageRequest.class))).thenReturn(questions);

        ResponseEntity<Page<QuestionSummaryResponse>> response = questionController.getAllQuestions(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        verify(questionService).getAllQuestions(any(PageRequest.class));
    }

    @Test
    void shouldGetQuestionByIdSuccessfully() {
        when(questionService.getQuestionbyId(anyLong(), any(), anyLong())).thenReturn(Optional.of(detailResponse));
        when(tokenUtil.getUserIdFromToken(any())).thenReturn(1L);

        ResponseEntity<QuestionDetailResponse> response = questionController.getQuestionById(1L, 0, 10, testToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(questionService).getQuestionbyId(eq(1L), any(), eq(1L));
    }


    @Test
    void shouldAskQuestionSuccessfully() {
        when(questionService.askQuestion(any(QuestionCreateRequest.class))).thenReturn(questionResponse);

        ResponseEntity<QuestionResponse> response = questionController.askQuestion(createRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(questionService).askQuestion(createRequest);
    }

    @Test
    void shouldUpdateQuestionSuccessfully() {
        when(questionService.updateQuestion(anyLong(), any(QuestionUpdateRequest.class))).thenReturn(true);

        ResponseEntity<MessageResponse> response = questionController.updateQuestion(1L, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Question updated successfully.", response.getBody().getMessage());
        verify(questionService).updateQuestion(1L, updateRequest);
    }


    @Test
    void shouldDeleteQuestionSuccessfully() {
        when(questionService.deleteQuestion(anyLong())).thenReturn(true);

        ResponseEntity<MessageResponse> response = questionController.deleteQuestion(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Question deleted successfully.", response.getBody().getMessage());
        verify(questionService).deleteQuestion(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentQuestion() {
        when(questionService.deleteQuestion(anyLong())).thenReturn(false);

        ResponseEntity<MessageResponse> response = questionController.deleteQuestion(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Question with ID 1 not found.", response.getBody().getMessage());
    }

    @Test
    void shouldGetQuestionsByUserSuccessfully() {
        List<QuestionSummaryResponse> questions = Arrays.asList(new QuestionSummaryResponse());
        when(questionService.getQuestionsByUser(anyLong())).thenReturn(Optional.of(questions));

        ResponseEntity<List<QuestionSummaryResponse>> response = questionController.getQuestionsByUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(questionService).getQuestionsByUser(1L);
    }

    @Test
    void shouldReturnNotFoundWhenNoQuestionsFoundForUser() {
        when(questionService.getQuestionsByUser(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<List<QuestionSummaryResponse>> response = questionController.getQuestionsByUser(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldUpvoteQuestionSuccessfully() {
        when(questionService.upvoteQuestion(anyLong(), anyLong())).thenReturn(new ReputationResponse());
        when(tokenUtil.getUserIdFromToken(any())).thenReturn(1L);

        ResponseEntity<ReputationResponse> response = questionController.upvoteQuestion(1L, testToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(questionService).upvoteQuestion(1L, 1L);
    }

    @Test
    void shouldDownvoteQuestionSuccessfully() {
        when(questionService.downvoteQuestion(anyLong(), anyLong())).thenReturn(new ReputationResponse());
        when(tokenUtil.getUserIdFromToken(any())).thenReturn(1L);

        ResponseEntity<ReputationResponse> response = questionController.downvoteQuestion(1L, testToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(questionService).downvoteQuestion(1L, 1L);
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalidForUpvote() {
        when(tokenUtil.getUserIdFromToken(any())).thenThrow(new InvalidAccessTokenException("Invalid token"));

        ResponseEntity<ReputationResponse> response = questionController.upvoteQuestion(1L, testToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalidForDownvote() {
        when(tokenUtil.getUserIdFromToken(any())).thenThrow(new InvalidAccessTokenException("Invalid token"));

        ResponseEntity<ReputationResponse> response = questionController.downvoteQuestion(1L, testToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

}

