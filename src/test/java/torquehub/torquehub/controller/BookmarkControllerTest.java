package torquehub.torquehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import torquehub.torquehub.business.interfaces.BookmarkService;
import torquehub.torquehub.configuration.SecurityConfig;
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.jwt.token.impl.AccessTokenEncoderDecoderImpl;
import torquehub.torquehub.configuration.jwt.token.impl.BlacklistService;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.controllers.BookmarkController;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkQuestionRequest;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkRequest;
import torquehub.torquehub.domain.response.bookmark_dtos.BookmarkResponse;

import java.util.Collections;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(BookmarkController.class)
@Import({SecurityConfig.class, AccessTokenEncoderDecoderImpl.class, BlacklistService.class})
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookmarkService bookmarkService;

    @MockBean
    private TokenUtil tokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccessTokenDecoder accessTokenDecoder;

    @MockBean
    private BlacklistService blacklistService;

    private static final String VALID_TOKEN = "Bearer valid-token";

    private BookmarkRequest validBookmarkRequest;
    private BookmarkResponse bookmarkResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new BookmarkController(bookmarkService, tokenUtil))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        validBookmarkRequest = new BookmarkRequest();
        validBookmarkRequest.setAnswerId(1L);
        validBookmarkRequest.setUserId(1L);
        validBookmarkRequest.setQuestionId(1L);

        bookmarkResponse = new BookmarkResponse();
        bookmarkResponse.setId(1L);
    }

    @Test
    @WithMockUser
    void shouldToggleBookmarkQuestion_whenValidRequest() throws Exception {
        given(tokenUtil.getUserIdFromToken(VALID_TOKEN)).willReturn(1L);
        given(bookmarkService.bookmarkQuestion(any(BookmarkQuestionRequest.class))).willReturn(bookmarkResponse);

        mockMvc.perform(post("/bookmarks/1")
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void shouldReturnInternalServerError_whenExceptionOccursInToggleBookmarkQuestion() throws Exception {
        given(tokenUtil.getUserIdFromToken(VALID_TOKEN)).willReturn(1L);
        doThrow(new RuntimeException("Error")).when(bookmarkService).bookmarkQuestion(any(BookmarkQuestionRequest.class));

        mockMvc.perform(post("/bookmarks/1")
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void shouldBookmarkAnswer_whenValidRequest() throws Exception {
        given(tokenUtil.getUserIdFromToken(VALID_TOKEN)).willReturn(1L);
        given(bookmarkService.bookmarkAnswer(any(BookmarkRequest.class))).willReturn(bookmarkResponse);

        validBookmarkRequest.setQuestionId(1L);

        mockMvc.perform(post("/bookmarks/answer")
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookmarkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void shouldReturnInternalServerError_whenExceptionOccursInBookmarkAnswer() throws Exception {
        given(tokenUtil.getUserIdFromToken(VALID_TOKEN)).willReturn(1L);
        doThrow(new RuntimeException("Error")).when(bookmarkService).bookmarkAnswer(any(BookmarkRequest.class));

        validBookmarkRequest.setQuestionId(1L);

        mockMvc.perform(post("/bookmarks/answer")
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBookmarkRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void shouldGetUserBookmarkedQuestions_whenValidRequest() throws Exception {
        given(tokenUtil.getUserIdFromToken(VALID_TOKEN)).willReturn(1L);
        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<BookmarkResponse> page = new PageImpl<>(Collections.singletonList(bookmarkResponse), pageable, 1);
        given(bookmarkService.getUserBookmarkedQuestions(eq(1L), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/bookmarks/questions/1")
                        .header("Authorization", VALID_TOKEN)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @WithMockUser
    void shouldGetUserBookmarkedAnswers_whenValidRequest() throws Exception {
        given(tokenUtil.getUserIdFromToken(VALID_TOKEN)).willReturn(1L);
        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<BookmarkResponse> page = new PageImpl<>(Collections.singletonList(bookmarkResponse), pageable, 1);
        given(bookmarkService.getUserBookmarkedAnswers(eq(1L), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/bookmarks/answers/1")
                        .header("Authorization", VALID_TOKEN)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @WithMockUser
    void shouldReturnForbidden_whenUserIdDoesNotMatchTokenForQuestions() throws Exception {
        given(tokenUtil.getUserIdFromToken(VALID_TOKEN)).willReturn(2L);

        mockMvc.perform(get("/bookmarks/questions/1")
                        .header("Authorization", VALID_TOKEN)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldReturnForbidden_whenUserIdDoesNotMatchTokenForAnswers() throws Exception {
        given(tokenUtil.getUserIdFromToken(VALID_TOKEN)).willReturn(2L);

        mockMvc.perform(get("/bookmarks/answers/1")
                        .header("Authorization", VALID_TOKEN)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}

