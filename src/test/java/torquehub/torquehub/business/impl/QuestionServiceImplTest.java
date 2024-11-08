package torquehub.torquehub.business.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import torquehub.torquehub.business.exeption.question_exeptions.QuestionCreationException;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.business.interfaces.VoteService;
import torquehub.torquehub.domain.mapper.AnswerMapper;
import torquehub.torquehub.domain.mapper.CommentMapper;
import torquehub.torquehub.domain.mapper.QuestionMapper;
import torquehub.torquehub.domain.model.jpa_models.*;
import torquehub.torquehub.domain.request.question_dtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.question_dtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.response.question_dtos.QuestionDetailResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;
import torquehub.torquehub.persistence.jpa.impl.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {
    @InjectMocks
    private QuestionServiceImpl questionService;

    @Mock
    private JpaQuestionRepository questionRepository;
    @Mock
    private JpaTagRepository tagRepository;
    @Mock
    private JpaUserRepository userRepository;
    @Mock
    private QuestionMapper questionMapper;
    @Mock
    private ReputationService reputationService;
    @Mock
    private JpaVoteRepository voteRepository;
    @Mock
    private VoteService voteService;
    @Mock
    private JpaFollowRepository followRepository;
    @Mock
    private JpaBookmarkRepository bookmarkRepository;

    private JpaQuestion testQuestion;
    private JpaUser testUser;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUser = new JpaUser();
        testUser.setId(1L);

        testQuestion = new JpaQuestion();
        testQuestion.setId(1L);
        testQuestion.setJpaUser(testUser);
        testQuestion.setViews(0);

        pageable = PageRequest.of(0, 10);
    }


    @Test
    void shouldAskQuestionSuccessfully() {
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setUserId(1L);
        request.setTitle("Sample Title");
        request.setDescription("Sample Description");
        request.setTags(Set.of("tag1", "tag2"));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(tagRepository.findByName(anyString())).thenReturn(Optional.of(new JpaTag()));
        when(questionRepository.save(any(JpaQuestion.class))).thenReturn(testQuestion);
        when(questionMapper.toResponse(any(JpaQuestion.class))).thenReturn(new QuestionResponse());

        QuestionResponse response = questionService.askQuestion(request);

        assertNotNull(response);
        verify(questionRepository).save(any(JpaQuestion.class));
    }

    @Test
    void shouldThrowQuestionCreationExceptionWhenAskQuestionFails() {
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setUserId(1L);
        request.setTags(Set.of("tag1", "tag2"));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(tagRepository.findByName(anyString())).thenReturn(Optional.empty());  // Simulate missing tag

        assertThrows(QuestionCreationException.class, () -> questionService.askQuestion(request));
    }

    @Test
    void shouldDeleteQuestionSuccessfully() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(testQuestion));
        when(reputationService.updateReputationForQuestionWhenQuestionIsDeleted(any())).thenReturn(true);

        assertTrue(questionService.deleteQuestion(1L));
        verify(questionRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeleteNonExistentQuestion() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(QuestionCreationException.class, () -> questionService.deleteQuestion(1L));
    }

    @Test
    void shouldUpdateQuestionSuccessfully() {
        QuestionUpdateRequest updateRequest = new QuestionUpdateRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setTags(Set.of("tag1", "tag2"));

        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(testQuestion));
        when(tagRepository.findByName(anyString())).thenReturn(Optional.of(new JpaTag()));

        assertTrue(questionService.updateQuestion(1L, updateRequest));
        verify(questionRepository).save(any(JpaQuestion.class));
    }

    @Test
    void shouldGetQuestionByIdSuccessfully() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(testQuestion));
        when(questionMapper.toDetailResponse(
                any(JpaQuestion.class),
                any(Pageable.class),
                nullable(CommentMapper.class),
                nullable(AnswerMapper.class),
                nullable(JpaBookmarkRepository.class),
                nullable(JpaFollowRepository.class),
                nullable(Long.class)
        )).thenReturn(new QuestionDetailResponse());

        Optional<QuestionDetailResponse> response = questionService.getQuestionbyId(1L, PageRequest.of(0, 10));

        assertTrue(response.isPresent());
    }

    @Test
    void shouldGetAllQuestionsSuccessfully() {
        Page<JpaQuestion> questionPage = new PageImpl<>(Collections.singletonList(testQuestion));
        when(questionRepository.findAll(any(Pageable.class))).thenReturn(questionPage);
        when(questionMapper.toSummaryResponse(any(JpaQuestion.class))).thenReturn(new QuestionSummaryResponse());

        Page<QuestionSummaryResponse> response = questionService.getAllQuestions(PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }
    @Test
    void shouldGetQuestionByIdWithUserSuccessfully() {
        JpaVote vote = new JpaVote();
        vote.setUpvote(true);

        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(testQuestion));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(voteRepository.findByUserAndJpaQuestion(any(), any())).thenReturn(Optional.of(vote));
        when(followRepository.findByUserIdAndQuestionId(anyLong(), anyLong())).thenReturn(Optional.of(new JpaFollow()));
        when(bookmarkRepository.findByUserIdAndJpaQuestionId(anyLong(), anyLong())).thenReturn(Optional.of(new JpaBookmark()));
        when(questionMapper.toDetailResponse(any(), any(), any(), any(), any(), any(), anyLong())).thenReturn(new QuestionDetailResponse());

        Optional<QuestionDetailResponse> response = questionService.getQuestionbyId(1L, pageable, 1L);

        assertTrue(response.isPresent());
        verify(voteRepository).findByUserAndJpaQuestion(any(), any());
        verify(followRepository).findByUserIdAndQuestionId(anyLong(), anyLong());
        verify(bookmarkRepository).findByUserIdAndJpaQuestionId(anyLong(), anyLong());
    }

    @Test
    void shouldGetQuestionByIdWithUserWhenNoVoteExists() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(testQuestion));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(voteRepository.findByUserAndJpaQuestion(any(), any())).thenReturn(Optional.empty());
        when(followRepository.findByUserIdAndQuestionId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(bookmarkRepository.findByUserIdAndJpaQuestionId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(questionMapper.toDetailResponse(any(), any(), any(), any(), any(), any(), any())).thenReturn(new QuestionDetailResponse());

        Optional<QuestionDetailResponse> response = questionService.getQuestionbyId(1L, pageable, 1L);

        assertTrue(response.isPresent());
    }

    @Test
    void shouldGetQuestionsByUserSuccessfully() {
        List<JpaQuestion> questions = Arrays.asList(testQuestion);
        when(questionRepository.findByJpaUserId(anyLong())).thenReturn(questions);
        when(questionMapper.toSummaryResponse(any(JpaQuestion.class))).thenReturn(new QuestionSummaryResponse());

        Optional<List<QuestionSummaryResponse>> response = questionService.getQuestionsByUser(1L);

        assertTrue(response.isPresent());
        assertEquals(1, response.get().size());
    }

    @Test
    void shouldReturnEmptyWhenNoQuestionsFoundForUser() {
        when(questionRepository.findByJpaUserId(anyLong())).thenReturn(Collections.emptyList());

        Optional<List<QuestionSummaryResponse>> response = questionService.getQuestionsByUser(1L);

        assertFalse(response.isPresent());
    }

    @Test
    void shouldUpvoteQuestionSuccessfully() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(testQuestion));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(voteService.handleUpvote(any(), any())).thenReturn(new ReputationResponse());

        ReputationResponse response = questionService.upvoteQuestion(1L, 1L);

        assertNotNull(response);
        verify(voteService).handleUpvote(testUser, testQuestion);
    }

    @Test
    void shouldDownvoteQuestionSuccessfully() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(testQuestion));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(voteService.handleDownvote(any(), any())).thenReturn(new ReputationResponse());

        ReputationResponse response = questionService.downvoteQuestion(1L, 1L);

        assertNotNull(response);
        verify(voteService).handleDownvote(testUser, testQuestion);
    }

    @Test
    void shouldIncrementQuestionViewSuccessfully() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(testQuestion));
        when(questionRepository.save(any(JpaQuestion.class))).thenReturn(testQuestion);

        boolean result = questionService.incrementQuestionView(1L);

        assertTrue(result);
        assertEquals(1, testQuestion.getViews());
        verify(questionRepository).save(testQuestion);
    }

    @Test
    void shouldThrowExceptionWhenIncrementViewForNonExistentQuestion() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(QuestionCreationException.class, () -> questionService.incrementQuestionView(1L));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentQuestion() {
        QuestionUpdateRequest updateRequest = new QuestionUpdateRequest();
        when(questionRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> questionService.updateQuestion(1L, updateRequest));
    }

    @Test
    void shouldThrowExceptionWhenTagNotFoundDuringUpdate() {
        QuestionUpdateRequest updateRequest = new QuestionUpdateRequest();
        updateRequest.setTags(Set.of("nonexistent-tag"));

        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(testQuestion));
        when(tagRepository.findByName(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> questionService.updateQuestion(1L, updateRequest));
    }

    @Test
    void shouldThrowExceptionWhenUpvotingNonExistentQuestion() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> questionService.upvoteQuestion(1L, 1L));
    }

    @Test
    void shouldThrowExceptionWhenDownvotingNonExistentQuestion() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> questionService.downvoteQuestion(1L, 1L));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForVoting() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(testQuestion));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> questionService.upvoteQuestion(1L, 1L));
    }

    @Test
    void shouldThrowExceptionWhenReputationUpdateFailsDuringDeletion() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(testQuestion));
        when(reputationService.updateReputationForQuestionWhenQuestionIsDeleted(any())).thenReturn(false);

        assertThrows(QuestionCreationException.class, () -> questionService.deleteQuestion(1L));
    }
}
