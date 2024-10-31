package torquehub.torquehub.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import torquehub.torquehub.business.interfaces.FilterService;
import torquehub.torquehub.configuration.jwt.auth.AuthenticationRequestFilter;
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.jwt.token.impl.BlacklistService;
import torquehub.torquehub.controllers.FilterController;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilterController.class)
@ImportAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
class FilterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FilterService filterService;

    @MockBean
    private AccessTokenDecoder accessTokenDecoder;

    private Page<QuestionSummaryResponse> mockPage;

    @MockBean
    private BlacklistService blacklistService;

    @MockBean
    private AuthenticationRequestFilter authenticationRequestFilter;

    @BeforeEach
    void setUp() {
        QuestionSummaryResponse mockResponse = new QuestionSummaryResponse();
        mockPage = new PageImpl<>(List.of(mockResponse));
    }

    @Test
    void testGetQuestionsByTags() throws Exception {
        when(filterService.getQuestionsByTags(eq(Set.of("java")), any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/questions/filter/tags")
                        .param("tags", "java")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetQuestionsByAskedTime() throws Exception {
        when(filterService.findAllByOrderByAskedTimeDesc(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/questions/filter/askedTime")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetQuestionsByLastActivity() throws Exception {
        when(filterService.findAllByOrderByLastActivityTimeDesc(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/questions/filter/lastActivity")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetQuestionsByVotes() throws Exception {
        when(filterService.findAllByOrderByVotesDesc(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/questions/filter/votes")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetQuestionsByViewCount() throws Exception {
        when(filterService.findAllByOrderByViewCountDesc(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/questions/filter/viewCount")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetQuestionsWithNoAnswers() throws Exception {
        when(filterService.findQuestionsWithNoAnswers(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/questions/filter/noAnswers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}

