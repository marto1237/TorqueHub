package torquehub.torquehub.business.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import torquehub.torquehub.domain.mapper.QuestionMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaQuestionRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaTagRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FilterServiceImplTest {

    @Mock
    private JpaQuestionRepository questionRepository;

    @Mock
    private JpaTagRepository tagRepository;

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private FilterServiceImpl filterService;

    private JpaQuestion mockQuestion;
    private QuestionSummaryResponse mockResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockQuestion = new JpaQuestion();
        mockResponse = new QuestionSummaryResponse();
    }

    @Test
    void testGetQuestionsByTags_Success() {
        JpaTag mockTag = new JpaTag();
        mockTag.setName("java");
        when(tagRepository.findByName("java")).thenReturn(Optional.of(mockTag));
        when(questionRepository.findQuestionsByTags(List.of(mockTag), PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(mockQuestion)));
        when(questionMapper.toSummaryResponse(mockQuestion)).thenReturn(mockResponse);

        Page<QuestionSummaryResponse> result = filterService.getQuestionsByTags(Set.of("java"), PageRequest.of(0, 10));
        assertEquals(1, result.getContent().size());
        assertEquals(mockResponse, result.getContent().get(0));
    }

    @Test
    void testGetQuestionsByTags_TagNotFound() {
        when(tagRepository.findByName("nonexistent")).thenReturn(Optional.empty());
        try {
            filterService.getQuestionsByTags(Set.of("nonexistent"), PageRequest.of(0, 10));
        } catch (IllegalArgumentException e) {
            assertEquals("Tag not found: nonexistent", e.getMessage());
        }
    }

    @Test
    void testFindAllByOrderByAskedTimeDesc() {
        when(questionRepository.findAllByOrderByAskedTimeDesc(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(mockQuestion)));
        when(questionMapper.toSummaryResponse(mockQuestion)).thenReturn(mockResponse);

        Page<QuestionSummaryResponse> result = filterService.findAllByOrderByAskedTimeDesc(PageRequest.of(0, 10));
        assertEquals(1, result.getContent().size());
        assertEquals(mockResponse, result.getContent().get(0));
    }

    @Test
    void testFindAllByOrderByLastActivityTimeDesc() {
        when(questionRepository.findAllByOrderByLastActivityTimeDesc(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(mockQuestion)));
        when(questionMapper.toSummaryResponse(mockQuestion)).thenReturn(mockResponse);

        Page<QuestionSummaryResponse> result = filterService.findAllByOrderByLastActivityTimeDesc(PageRequest.of(0, 10));
        assertEquals(1, result.getContent().size());
        assertEquals(mockResponse, result.getContent().get(0));
    }

    @Test
    void testFindAllByOrderByVotesDesc() {
        when(questionRepository.findAllByOrderByVotesDesc(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(mockQuestion)));
        when(questionMapper.toSummaryResponse(mockQuestion)).thenReturn(mockResponse);

        Page<QuestionSummaryResponse> result = filterService.findAllByOrderByVotesDesc(PageRequest.of(0, 10));
        assertEquals(1, result.getContent().size());
        assertEquals(mockResponse, result.getContent().get(0));
    }

    @Test
    void testFindAllByOrderByViewCountDesc() {
        when(questionRepository.findAllByOrderByViewCountDesc(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(mockQuestion)));
        when(questionMapper.toSummaryResponse(mockQuestion)).thenReturn(mockResponse);

        Page<QuestionSummaryResponse> result = filterService.findAllByOrderByViewCountDesc(PageRequest.of(0, 10));
        assertEquals(1, result.getContent().size());
        assertEquals(mockResponse, result.getContent().get(0));
    }

    @Test
    void testFindQuestionsWithNoAnswers() {
        when(questionRepository.findQuestionsWithNoAnswers(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(mockQuestion)));
        when(questionMapper.toSummaryResponse(mockQuestion)).thenReturn(mockResponse);

        Page<QuestionSummaryResponse> result = filterService.findQuestionsWithNoAnswers(PageRequest.of(0, 10));
        assertEquals(1, result.getContent().size());
        assertEquals(mockResponse, result.getContent().get(0));
    }

    @Test
    void testGetQuestionsByTags_TagNotFoundException() {
        // Arrange
        String nonexistentTag = "nonexistent";
        when(tagRepository.findByName(nonexistentTag)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> invokeGetQuestionsByTags(nonexistentTag) // Single invocation in lambda
        );

        // Verify exception message
        assertEquals("Tag not found: nonexistent", exception.getMessage());
    }

    // Helper method to encapsulate the potentially exception-throwing code
    private void invokeGetQuestionsByTags(String tag) {
        filterService.getQuestionsByTags(Set.of(tag), PageRequest.of(0, 10));
    }

}

