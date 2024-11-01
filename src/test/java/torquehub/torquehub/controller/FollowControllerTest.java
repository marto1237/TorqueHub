package torquehub.torquehub.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import torquehub.torquehub.business.interfaces.FollowService;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.controllers.FollowController;
import torquehub.torquehub.domain.request.follow_dtos.FollowAnswerRequest;
import torquehub.torquehub.domain.request.follow_dtos.FollowQuestionRequest;
import torquehub.torquehub.domain.request.follow_dtos.FollowedAnswerRequest;
import torquehub.torquehub.domain.request.follow_dtos.FollowedQuestionRequest;
import torquehub.torquehub.domain.response.follow_dtos.FollowResponse;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FollowControllerTest {
    @InjectMocks
    private FollowController followController;

    @Mock
    private FollowService followService;

    @Mock
    private TokenUtil tokenUtil;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldToggleFollowQuestionSuccessfully() {
        Long userId = 1L;
        Long questionId = 2L;
        FollowResponse mockResponse = new FollowResponse();
        String token = "Bearer token";

        when(tokenUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(followService.toggleFollowQuestion(any(FollowQuestionRequest.class))).thenReturn(mockResponse);

        ResponseEntity<FollowResponse> response = followController.toggleFollowQuestion(questionId, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(followService).toggleFollowQuestion(any(FollowQuestionRequest.class));
    }

    @Test
    void shouldReturnUnauthorized_whenTokenIsInvalid_forToggleFollowQuestion() {
        Long questionId = 2L;
        String token = "Bearer token";

        when(tokenUtil.getUserIdFromToken(token)).thenThrow(new RuntimeException("Invalid token"));

        ResponseEntity<FollowResponse> response = followController.toggleFollowQuestion(questionId, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldFollowAnswerSuccessfully() {
        Long userId = 1L;
        Long answerId = 2L;
        FollowResponse mockResponse = new FollowResponse();
        String token = "Bearer token";

        when(tokenUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(followService.toggleFollowAnswer(any(FollowAnswerRequest.class))).thenReturn(mockResponse);

        ResponseEntity<FollowResponse> response = followController.followAnswer(answerId, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void shouldReturnFollowedQuestionsSuccessfully() {
        Long userId = 1L;
        String token = "Bearer token";
        FollowResponse mockFollowResponse = new FollowResponse();
        Page<FollowResponse> mockPage = new PageImpl<>(List.of(mockFollowResponse));

        when(tokenUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(followService.getFollowedQuestions(any(FollowedQuestionRequest.class))).thenReturn(mockPage);

        ResponseEntity<Page<FollowResponse>> response = followController.getFollowedQuestions(token, 0, 5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPage, response.getBody());
    }

    @Test
    void shouldReturnFollowedAnswersSuccessfully() {
        Long userId = 1L;
        String token = "Bearer token";
        FollowResponse mockFollowResponse = new FollowResponse();
        Page<FollowResponse> mockPage = new PageImpl<>(List.of(mockFollowResponse));

        when(tokenUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(followService.getFollowedAnswers(any(FollowedAnswerRequest.class))).thenReturn(mockPage);

        ResponseEntity<Page<FollowResponse>> response = followController.getFollowedAnswers(token, 0, 5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPage, response.getBody());
    }

    @Test
    void shouldMuteBatchFollowsSuccessfully() {
        String token = "Bearer token";
        List<Long> followIds = Arrays.asList(1L, 2L);

        when(followService.batchMuteFollows(followIds)).thenReturn(true);

        ResponseEntity<Void> response = followController.batchMuteFollows(token, followIds);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(followService).batchMuteFollows(followIds);
    }

    @Test
    void shouldUnfollowBatchSuccessfully() {
        String token = "Bearer token";
        List<Long> followIds = Arrays.asList(1L, 2L);

        when(followService.batchUnfollow(followIds)).thenReturn(true);

        ResponseEntity<Void> response = followController.batchUnfollow(token, followIds);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(followService).batchUnfollow(followIds);
    }

    @Test
    void shouldHandleExceptionInToggleFollowQuestion() {
        Long questionId = 2L;
        String token = "Bearer token";

        when(tokenUtil.getUserIdFromToken(token)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<FollowResponse> response = followController.toggleFollowQuestion(questionId, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldHandleExceptionInFollowAnswer() {
        Long answerId = 2L;
        String token = "Bearer token";

        when(tokenUtil.getUserIdFromToken(token)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<FollowResponse> response = followController.followAnswer(answerId, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldHandleExceptionInGetFollowedQuestions() {
        String token = "Bearer token";

        when(tokenUtil.getUserIdFromToken(token)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Page<FollowResponse>> response = followController.getFollowedQuestions(token, 0, 5);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldHandleExceptionInGetFollowedAnswers() {
        String token = "Bearer token";

        when(tokenUtil.getUserIdFromToken(token)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Page<FollowResponse>> response = followController.getFollowedAnswers(token, 0, 5);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldHandleExceptionInBatchMuteFollows() {
        String token = "Bearer token";
        List<Long> followIds = Arrays.asList(1L, 2L);

        when(followService.batchMuteFollows(followIds)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Void> response = followController.batchMuteFollows(token, followIds);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void shouldHandleExceptionInBatchUnfollow() {
        String token = "Bearer token";
        List<Long> followIds = Arrays.asList(1L, 2L);

        when(followService.batchUnfollow(followIds)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Void> response = followController.batchUnfollow(token, followIds);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
