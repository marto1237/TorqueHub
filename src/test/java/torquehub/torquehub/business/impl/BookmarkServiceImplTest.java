package torquehub.torquehub.business.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import torquehub.torquehub.domain.mapper.BookmarkMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.model.jpa_models.JpaBookmark;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkQuestionRequest;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkRequest;
import torquehub.torquehub.domain.response.bookmark_dtos.BookmarkResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaAnswerRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaBookmarkRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaQuestionRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaUserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookmarkServiceImplTest {

    @Mock
    private JpaBookmarkRepository bookmarkRepository;

    @Mock
    private JpaAnswerRepository answerRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private JpaQuestionRepository questionRepository;

    @Mock
    private BookmarkMapper bookmarkMapper;

    @InjectMocks
    private BookmarkServiceImpl bookmarkService;

    private JpaUser testUser;
    private JpaQuestion testQuestion;
    private JpaAnswer testAnswer;
    private JpaBookmark testBookmark;
    private BookmarkResponse bookmarkResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = JpaUser.builder().id(1L).username("testUser").build();
        testQuestion = JpaQuestion.builder().id(1L).title("Test Question").build();
        testAnswer = JpaAnswer.builder().id(1L).text("Test Answer").build();
        testBookmark = JpaBookmark.builder().id(1L).jpaUser(testUser).jpaQuestion(testQuestion).createdAt(LocalDateTime.now()).build();
        bookmarkResponse = new BookmarkResponse();
        bookmarkResponse.setId(1L);
    }

    @Test
    void bookmarkQuestion_shouldBookmarkSuccessfully() {
        BookmarkQuestionRequest request = new BookmarkQuestionRequest(1L, 1L);
        when(bookmarkRepository.findByUserIdAndJpaQuestionId(1L, 1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(testQuestion));
        when(bookmarkRepository.save(any(JpaBookmark.class))).thenReturn(testBookmark);
        when(bookmarkMapper.toResponse(any(JpaBookmark.class))).thenReturn(bookmarkResponse);

        BookmarkResponse result = bookmarkService.bookmarkQuestion(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookmarkRepository).save(any(JpaBookmark.class));
    }

    @Test
    void bookmarkQuestion_shouldUnbookmarkIfExists() {
        BookmarkQuestionRequest request = new BookmarkQuestionRequest(1L, 1L);
        when(bookmarkRepository.findByUserIdAndJpaQuestionId(1L, 1L)).thenReturn(Optional.of(testBookmark));

        BookmarkResponse result = bookmarkService.bookmarkQuestion(request);

        assertNull(result);
        verify(bookmarkRepository).delete(testBookmark);
    }

    @Test
    void bookmarkQuestion_shouldThrowExceptionIfUserNotFound() {
        BookmarkQuestionRequest request = new BookmarkQuestionRequest(1L, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bookmarkService.bookmarkQuestion(request));
    }

    @Test
    void bookmarkAnswer_shouldBookmarkSuccessfully() {
        BookmarkRequest request = new BookmarkRequest(1L, 1L, 1L);
        when(bookmarkRepository.findByUserIdAndJpaAnswerId(1L, 1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(answerRepository.findById(1L)).thenReturn(Optional.of(testAnswer));
        when(bookmarkRepository.save(any(JpaBookmark.class))).thenReturn(testBookmark);
        when(bookmarkMapper.toResponse(any(JpaBookmark.class))).thenReturn(bookmarkResponse);

        BookmarkResponse result = bookmarkService.bookmarkAnswer(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookmarkRepository).save(any(JpaBookmark.class));
    }

    @Test
    void bookmarkAnswer_shouldUnbookmarkIfExists() {
        BookmarkRequest request = new BookmarkRequest(1L, 1L, 1L);
        when(bookmarkRepository.findByUserIdAndJpaAnswerId(1L, 1L)).thenReturn(Optional.of(testBookmark));

        BookmarkResponse result = bookmarkService.bookmarkAnswer(request);

        assertNull(result);
        verify(bookmarkRepository).delete(testBookmark);
    }

    @Test
    void bookmarkAnswer_shouldThrowExceptionIfAnswerNotFound() {
        BookmarkRequest request = new BookmarkRequest(1L, 1L, 1L);
        when(answerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bookmarkService.bookmarkAnswer(request));
    }

    @Test
    void getUserBookmarkedQuestions_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<JpaBookmark> page = new PageImpl<>(Collections.singletonList(testBookmark));
        when(bookmarkRepository.findByUserIdAndJpaQuestionIsNotNull(1L, pageable)).thenReturn(page);
        when(bookmarkMapper.toResponse(any(JpaBookmark.class))).thenReturn(bookmarkResponse);

        Page<BookmarkResponse> result = bookmarkService.getUserBookmarkedQuestions(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
    }

    @Test
    void getUserBookmarkedAnswers_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<JpaBookmark> page = new PageImpl<>(Collections.singletonList(testBookmark));
        when(bookmarkRepository.findByUserIdAndJpaAnswerIsNotNull(1L, pageable)).thenReturn(page);
        when(bookmarkMapper.toResponse(any(JpaBookmark.class))).thenReturn(bookmarkResponse);

        Page<BookmarkResponse> result = bookmarkService.getUserBookmarkedAnswers(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
    }
}
