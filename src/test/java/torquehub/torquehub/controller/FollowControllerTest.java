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
import torquehub.torquehub.domain.request.follow_dtos.*;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.follow_dtos.FollowResponse;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    void shouldMuteFollowSuccessfullyInController() {
        String token = "Bearer token";
        Long followId = 1L;

        when(tokenUtil.getUserIdFromToken(token)).thenReturn(1L);
        when(followService.muteFollow(any(MuteFollowRequest.class))).thenReturn(true);

        ResponseEntity<MessageResponse> response = followController.toggleMute(followId, true, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Follow muted successfully", response.getBody().getMessage());
        verify(followService).muteFollow(any(MuteFollowRequest.class));
    }

    @Test
    void shouldHandleExceptionWhenMuteFollowFails() {
        String token = "Bearer token";
        Long followId = 1L;

        when(tokenUtil.getUserIdFromToken(token)).thenReturn(1L);
        doThrow(new RuntimeException("Internal error")).when(followService).muteFollow(any(MuteFollowRequest.class));

        ResponseEntity<MessageResponse> response = followController.toggleMute(followId, true, token);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldBatchMuteFollowsSuccessfullyInController() {
        String token = "Bearer token";
        List<MuteFollowRequest> muteRequests = List.of(
                new MuteFollowRequest(1L, 1L, true),
                new MuteFollowRequest(1L, 2L, false)
        );

        when(tokenUtil.getUserIdFromToken(token)).thenReturn(1L);
        when(followService.batchToggleMuteFollows(anyList())).thenReturn(true);

        ResponseEntity<MessageResponse> response = followController.batchToggleMuteFollows(muteRequests, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Follows muted/unmuted successfully", response.getBody().getMessage());
        verify(followService).batchToggleMuteFollows(anyList());
    }

    @Test
    void shouldHandleExceptionWhenBatchMuteFollowsFails() {
        String token = "Bearer token";
        List<MuteFollowRequest> muteRequests = List.of(
                new MuteFollowRequest(1L, 1L, true)
        );

        when(tokenUtil.getUserIdFromToken(token)).thenReturn(1L);
        doThrow(new RuntimeException("Internal error")).when(followService).batchToggleMuteFollows(anyList());

        ResponseEntity<MessageResponse> response = followController.batchToggleMuteFollows(muteRequests, token);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}
