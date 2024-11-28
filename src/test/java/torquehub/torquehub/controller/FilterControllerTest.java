package torquehub.torquehub.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import torquehub.torquehub.business.interfaces.FilterService;
import torquehub.torquehub.controllers.FilterController;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FilterControllerTest {

    @Mock
    private FilterService filterService;

    @InjectMocks
    private FilterController filterController;

    private List<QuestionSummaryResponse> questionList;
    private Page<QuestionSummaryResponse> questionPage;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        QuestionSummaryResponse question1 = new QuestionSummaryResponse();
        QuestionSummaryResponse question2 = new QuestionSummaryResponse();
        questionList = Arrays.asList(question1, question2);
        pageable = PageRequest.of(0, 10);
        questionPage = new PageImpl<>(questionList, pageable, questionList.size());
    }

    @Test
    void getQuestionsByTags_Success() {
        // Arrange
        Set<String> tags = new HashSet<>(Arrays.asList("java", "spring"));
        when(filterService.getQuestionsByTags(eq(tags), any(Pageable.class)))
                .thenReturn(questionPage);

        // Act
        ResponseEntity<Page<QuestionSummaryResponse>> response =
                filterController.getQuestionsByTags(tags, 0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        verify(filterService).getQuestionsByTags(eq(tags), any(Pageable.class));
    }

    @Test
    void getQuestionsByAskedTime_Success() {
        // Arrange
        when(filterService.findAllByOrderByAskedTimeDesc(any(Pageable.class)))
                .thenReturn(questionPage);

        // Act
        ResponseEntity<Page<QuestionSummaryResponse>> response =
                filterController.getQuestionsByAskedTime(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        verify(filterService).findAllByOrderByAskedTimeDesc(any(Pageable.class));
    }

    @Test
    void getQuestionsByLastActivity_Success() {
        // Arrange
        when(filterService.findAllByOrderByLastActivityTimeDesc(any(Pageable.class)))
                .thenReturn(questionPage);

        // Act
        ResponseEntity<Page<QuestionSummaryResponse>> response =
                filterController.getQuestionsByLastActivity(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        verify(filterService).findAllByOrderByLastActivityTimeDesc(any(Pageable.class));
    }

    @Test
    void getQuestionsByVotes_Success() {
        // Arrange
        when(filterService.findAllByOrderByVotesDesc(any(Pageable.class)))
                .thenReturn(questionPage);

        // Act
        ResponseEntity<Page<QuestionSummaryResponse>> response =
                filterController.getQuestionsByVotes(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        verify(filterService).findAllByOrderByVotesDesc(any(Pageable.class));
    }

    @Test
    void getQuestionsByViewCount_Success() {
        // Arrange
        when(filterService.findAllByOrderByViewCountDesc(any(Pageable.class)))
                .thenReturn(questionPage);

        // Act
        ResponseEntity<Page<QuestionSummaryResponse>> response =
                filterController.getQuestionsByViewCount(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        verify(filterService).findAllByOrderByViewCountDesc(any(Pageable.class));
    }

    @Test
    void getQuestionsWithNoAnswers_Success() {
        // Arrange
        when(filterService.findQuestionsWithNoAnswers(any(Pageable.class)))
                .thenReturn(questionPage);

        // Act
        ResponseEntity<Page<QuestionSummaryResponse>> response =
                filterController.getQuestionsWithNoAnswers(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        verify(filterService).findQuestionsWithNoAnswers(any(Pageable.class));
    }

    @Test
    void filterQuestions_Success() {
        // Arrange
        Set<String> tags = new HashSet<>(Arrays.asList("java", "spring"));
        Boolean noAnswers = true;
        Boolean noAcceptedAnswer = false;
        String sortOption = "newest";

        when(filterService.filterQuestions(
                eq(tags),
                eq(noAnswers),
                eq(noAcceptedAnswer),
                eq(sortOption),
                any(Pageable.class)
        )).thenReturn(questionPage);

        // Act
        Page<QuestionSummaryResponse> response = filterController.filterQuestions(
                tags,
                noAnswers,
                noAcceptedAnswer,
                sortOption,
                pageable
        );

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        verify(filterService).filterQuestions(
                eq(tags),
                eq(noAnswers),
                eq(noAcceptedAnswer),
                eq(sortOption),
                any(Pageable.class)
        );
    }

}