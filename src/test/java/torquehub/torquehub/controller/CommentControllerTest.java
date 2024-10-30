package torquehub.torquehub.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import torquehub.torquehub.business.exeption.comment_exeptions.CommentNotFoundException;
import torquehub.torquehub.business.interfaces.CommentService;
import torquehub.torquehub.configuration.SecurityConfig;
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.configuration.jwt.token.impl.AccessTokenEncoderDecoderImpl;
import torquehub.torquehub.configuration.jwt.token.impl.BlacklistService;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.controllers.CommentController;
import torquehub.torquehub.domain.response.comment_dtos.CommentResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@Import({SecurityConfig.class, AccessTokenEncoderDecoderImpl.class, BlacklistService.class})
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private TokenUtil tokenUtil;

    @MockBean
    private AccessTokenDecoder accessTokenDecoder;

    @MockBean
    private BlacklistService blacklistService;

    private static final String VALID_TOKEN = "Bearer valid-token";


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        AccessToken mockAccessToken = mock(AccessToken.class);
        given(mockAccessToken.getUsername()).willReturn("user");
        given(mockAccessToken.getRole()).willReturn("MODERATOR");

        // Mock valid and invalid token decoding scenarios
        when(accessTokenDecoder.decode("valid-token")).thenReturn(mockAccessToken);
        when(tokenUtil.getUserIdFromToken("Bearer valid-token")).thenReturn(1L);

        // Simulate invalid token behavior
        when(accessTokenDecoder.decode("invalid-token")).thenThrow(new InvalidAccessTokenException("Invalid token"));
    }


    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void shouldReturnComment_whenCommentExists() throws Exception {
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(1L);
        commentResponse.setText("Sample comment");

        when(commentService.getCommentById(1L)).thenReturn(commentResponse);

        mockMvc.perform(get("/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Sample comment"));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void shouldReturnNotFound_whenCommentDoesNotExist() throws Exception {
        when(commentService.getCommentById(1L)).thenThrow(new CommentNotFoundException("Comment not found"));

        mockMvc.perform(get("/comments/1"))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void shouldReturnPaginatedCommentsByAnswer() throws Exception {
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(1L);
        commentResponse.setText("Sample comment");

        when(commentService.getPaginatedComments(eq(1L), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/comments/answer/1")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void shouldAddComment_whenRequestIsValid() throws Exception {
        String token = "Bearer valid-token";
        when(tokenUtil.getUserIdFromToken("Bearer valid-token")).thenReturn(1L);

        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(1L);
        commentResponse.setText("New comment");

        when(commentService.addComment(any())).thenReturn(commentResponse);

        mockMvc.perform(post("/comments")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"New comment\", \"userId\":1, \"answerId\":1}")) // Ensure all fields are included
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("New comment"));
    }


    @Test
    void shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        when(tokenUtil.getUserIdFromToken(any())).thenThrow(new InvalidAccessTokenException("Invalid token"));

        mockMvc.perform(post("/comments")
                        .header("Authorization", "Bearer invalidToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"New comment\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void shouldEditComment_whenUserIsAuthorized() throws Exception {
        String token = "Bearer valid-token";
        when(tokenUtil.getUserIdFromToken("Bearer valid-token")).thenReturn(1L);

        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(1L);
        commentResponse.setText("Updated comment");

        when(commentService.editComment(eq(1L), any())).thenReturn(commentResponse);

        mockMvc.perform(put("/comments/1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Updated comment\", \"userId\":1}")) // Include userId if required
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Updated comment"));
    }


    @Test
    void shouldReturnUnauthorized_whenEditingCommentWithoutValidToken() throws Exception {
        when(tokenUtil.getUserIdFromToken(any())).thenThrow(new InvalidAccessTokenException("Invalid token"));

        mockMvc.perform(put("/comments/1")
                        .header("Authorization", "Bearer invalidToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Updated comment\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void shouldDeleteComment_whenUserIsAuthorized() throws Exception {
        String token = "Bearer valid-token";
        when(tokenUtil.getUserIdFromToken("Bearer valid-token")).thenReturn(1L);
        when(commentService.deleteComment(1L)).thenReturn(true);

        mockMvc.perform(delete("/comments/1")
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }


    @Test
    void shouldReturnUnauthorized_whenDeletingCommentWithInvalidToken() throws Exception {
        when(tokenUtil.getUserIdFromToken(any())).thenThrow(new InvalidAccessTokenException("Invalid token"));

        mockMvc.perform(delete("/comments/1")
                        .header("Authorization", "Bearer invalidToken"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void shouldReturnBadRequest_whenCommentDeletionFails() throws Exception {
        String token = "Bearer valid-token";
        when(tokenUtil.getUserIdFromToken("Bearer valid-token")).thenReturn(1L);
        when(commentService.deleteComment(1L)).thenReturn(false);

        mockMvc.perform(delete("/comments/1")
                        .header("Authorization", token))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void shouldReturnUserComments_whenUserExists() throws Exception {
        List<CommentResponse> comments = Arrays.asList(
                createCommentResponse(1L, "Comment 1"),
                createCommentResponse(2L, "Comment 2")
        );

        when(commentService.getCommentsByUser(1L)).thenReturn(Optional.of(comments));

        mockMvc.perform(get("/comments/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void shouldReturnNotFound_whenUserHasNoComments() throws Exception {
        when(commentService.getCommentsByUser(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/comments/user/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void shouldReturnAllCommentsByAnswer_whenAnswerExists() throws Exception {
        List<CommentResponse> comments = Arrays.asList(
                createCommentResponse(1L, "Comment 1"),
                createCommentResponse(2L, "Comment 2")
        );

        when(commentService.getCommentsByAnswer(1L)).thenReturn(Optional.of(comments));

        mockMvc.perform(get("/comments/answer/1/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void shouldReturnNotFound_whenAnswerHasNoComments() throws Exception {
        when(commentService.getCommentsByAnswer(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/comments/answer/1/all"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void shouldUpvoteComment_whenRequestIsValid() throws Exception {
        ReputationResponse reputationResponse = new ReputationResponse();
        reputationResponse.setUpdatedReputationPoints(10);

        when(tokenUtil.getUserIdFromToken(VALID_TOKEN)).thenReturn(1L);
        when(commentService.upvoteComment(1L, 1L)).thenReturn(reputationResponse);

        mockMvc.perform(post("/comments/1/upvote")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedReputationPoints").value(10));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void shouldReturnInternalServerError_whenUpvoteFails() throws Exception {
        when(tokenUtil.getUserIdFromToken(VALID_TOKEN)).thenReturn(1L);
        when(commentService.upvoteComment(1L, 1L)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/comments/1/upvote")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void shouldDownvoteComment_whenRequestIsValid() throws Exception {
        ReputationResponse reputationResponse = new ReputationResponse();
        reputationResponse.setUpdatedReputationPoints(-5);

        when(tokenUtil.getUserIdFromToken(VALID_TOKEN)).thenReturn(1L);
        when(commentService.downvoteComment(1L, 1L)).thenReturn(reputationResponse);

        mockMvc.perform(post("/comments/1/downvote")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedReputationPoints").value(-5));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void shouldReturnInternalServerError_whenDownvoteFails() throws Exception {
        when(tokenUtil.getUserIdFromToken(VALID_TOKEN)).thenReturn(1L);
        when(commentService.downvoteComment(1L, 1L)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/comments/1/downvote")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void addComment_ShouldReturn500_WhenServiceFails() throws Exception {
        when(tokenUtil.getUserIdFromToken(VALID_TOKEN)).thenReturn(1L);
        when(commentService.addComment(any())).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/comments")
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"New comment\", \"answerId\":1, \"userId\":1}")) // Added userId to make request valid
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void editComment_ShouldReturn500_WhenTokenValidationFails() throws Exception {
        when(tokenUtil.getUserIdFromToken(VALID_TOKEN))
                .thenThrow(new RuntimeException("Token validation failed"));

        mockMvc.perform(put("/comments/1")
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Updated comment\", \"userId\":1}")) // Added userId to make request valid
                .andExpect(status().isInternalServerError());
    }

    private CommentResponse createCommentResponse(Long id, String text) {
        CommentResponse response = new CommentResponse();
        response.setId(id);
        response.setText(text);
        return response;
    }


}
