package torquehub.torquehub.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import torquehub.torquehub.business.interfaces.AnswerService;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.controllers.AnswerController;
import torquehub.torquehub.controllers.websocketcontrollers.WebSocketAnswerController;
import torquehub.torquehub.domain.request.answer_dtos.AnswerCreateRequest;
import torquehub.torquehub.domain.request.answer_dtos.AnswerEditRequest;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class AnswerControllerTest {

    @Mock
    private AnswerService answerService;

    @Mock
    private WebSocketAnswerController webSocketAnswerController;

    @Mock
    private TokenUtil tokenUtil;

    @InjectMocks
    private AnswerController answerController;

    private static final String VALID_TOKEN = "Bearer valid_token";
    private static final Long USER_ID = 1L;
    private static final Long ANSWER_ID = 1L;
    private static final Long QUESTION_ID = 1L;

    @BeforeEach
    void setUp() {
        // Default valid token behavior
        when(tokenUtil.getUserIdFromToken(VALID_TOKEN)).thenReturn(USER_ID);
    }

    @Test
    void createAnswer_Success() {
        AnswerCreateRequest request = new AnswerCreateRequest();
        request.setQuestionId(QUESTION_ID);
        AnswerResponse expectedResponse = new AnswerResponse();
        when(answerService.addAnswer(any())).thenReturn(expectedResponse);

        ResponseEntity<AnswerResponse> response = answerController.createAnswer(request, VALID_TOKEN);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(webSocketAnswerController).notifyClients(eq(QUESTION_ID), any(AnswerResponse.class));
    }

    @Test
    void createAnswer_InvalidToken() {
        when(tokenUtil.getUserIdFromToken(VALID_TOKEN))
                .thenThrow(new InvalidAccessTokenException("Invalid token"));
        AnswerCreateRequest request = new AnswerCreateRequest();

        ResponseEntity<AnswerResponse> response = answerController.createAnswer(request, VALID_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void upvoteAnswer_Success() {
        ReputationResponse expectedResponse = new ReputationResponse();
        when(answerService.upvoteAnswer(ANSWER_ID, USER_ID)).thenReturn(expectedResponse);

        ResponseEntity<ReputationResponse> response = answerController.upvoteAnswer(ANSWER_ID, VALID_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void upvoteAnswer_InvalidToken() {
        when(tokenUtil.getUserIdFromToken(VALID_TOKEN))
                .thenThrow(new InvalidAccessTokenException("Invalid token"));

        ResponseEntity<ReputationResponse> response = answerController.upvoteAnswer(ANSWER_ID, VALID_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void downvoteAnswer_Success() {
        ReputationResponse expectedResponse = new ReputationResponse();
        when(answerService.downvoteAnswer(ANSWER_ID, USER_ID)).thenReturn(expectedResponse);

        ResponseEntity<ReputationResponse> response = answerController.downvoteAnswer(ANSWER_ID, VALID_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void downvoteAnswer_InvalidToken() {
        when(tokenUtil.getUserIdFromToken(VALID_TOKEN))
                .thenThrow(new InvalidAccessTokenException("Invalid token"));

        ResponseEntity<ReputationResponse> response = answerController.downvoteAnswer(ANSWER_ID, VALID_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void approveBestAnswer_Success() {
        ReputationResponse expectedResponse = new ReputationResponse();
        when(answerService.approveBestAnswer(QUESTION_ID, ANSWER_ID, USER_ID)).thenReturn(expectedResponse);

        ResponseEntity<ReputationResponse> response = answerController.approveBestAnswer(QUESTION_ID, ANSWER_ID, VALID_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void approveBestAnswer_InvalidToken() {
        when(tokenUtil.getUserIdFromToken(VALID_TOKEN))
                .thenThrow(new InvalidAccessTokenException("Invalid token"));

        ResponseEntity<ReputationResponse> response = answerController.approveBestAnswer(QUESTION_ID, ANSWER_ID, VALID_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void editAnswer_Success() {
        AnswerEditRequest request = new AnswerEditRequest();
        AnswerResponse expectedResponse = new AnswerResponse();
        when(answerService.editAnswer(eq(ANSWER_ID), any())).thenReturn(expectedResponse);

        ResponseEntity<AnswerResponse> response = answerController.editAnswer(ANSWER_ID, request, VALID_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void editAnswer_InvalidToken() {
        when(tokenUtil.getUserIdFromToken(VALID_TOKEN))
                .thenThrow(new InvalidAccessTokenException("Invalid token"));
        AnswerEditRequest request = new AnswerEditRequest();

        ResponseEntity<AnswerResponse> response = answerController.editAnswer(ANSWER_ID, request, VALID_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getAnswersByQuestion_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<AnswerResponse> answers = Arrays.asList(new AnswerResponse(), new AnswerResponse());
        Page<AnswerResponse> expectedPage = new PageImpl<>(answers);
        when(answerService.getAnswersByQuestion(QUESTION_ID, pageable)).thenReturn(expectedPage);

        ResponseEntity<Page<AnswerResponse>> response = answerController.getAnswersByQuestion(QUESTION_ID, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPage, response.getBody());
    }

    @Test
    void getAnswersByUser_Success() {
        List<AnswerResponse> expectedAnswers = Arrays.asList(new AnswerResponse(), new AnswerResponse());
        when(answerService.getAnswersByUser(USER_ID)).thenReturn(Optional.of(expectedAnswers));

        ResponseEntity<List<AnswerResponse>> response = answerController.getAnswersByUser(USER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedAnswers, response.getBody());
    }

    @Test
    void getAnswersByUser_NotFound() {
        when(answerService.getAnswersByUser(USER_ID)).thenReturn(Optional.empty());

        ResponseEntity<List<AnswerResponse>> response = answerController.getAnswersByUser(USER_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getAnswerById_Success() {
        AnswerResponse expectedResponse = new AnswerResponse();
        when(answerService.getAnswerById(ANSWER_ID)).thenReturn(expectedResponse);

        ResponseEntity<AnswerResponse> response = answerController.getAnswerById(ANSWER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void deleteAnswer_Success() {
        when(answerService.deleteAnswer(ANSWER_ID)).thenReturn(true);

        ResponseEntity<MessageResponse> response = answerController.deleteAnswer(ANSWER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Answer deleted successfully.", response.getBody().getMessage());
    }

    @Test
    void deleteAnswer_NotFound() {
        when(answerService.deleteAnswer(ANSWER_ID)).thenReturn(false);

        ResponseEntity<MessageResponse> response = answerController.deleteAnswer(ANSWER_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Answer with ID " + ANSWER_ID + " not found.", response.getBody().getMessage());
    }
}