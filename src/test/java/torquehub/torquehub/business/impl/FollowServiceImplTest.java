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
import torquehub.torquehub.business.exeption.filter_exeption.FilterFollowAnswerExeption;
import torquehub.torquehub.business.exeption.filter_exeption.FilterFollowQuestionExeption;
import torquehub.torquehub.domain.mapper.FollowMapper;
import torquehub.torquehub.domain.model.jpa_models.*;
import torquehub.torquehub.domain.request.follow_dtos.*;
import torquehub.torquehub.domain.response.follow_dtos.FollowResponse;
import torquehub.torquehub.persistence.jpa.impl.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FollowServiceImplTest {

    @InjectMocks
    private FollowServiceImpl followService;

    @Mock
    private JpaFollowRepository followRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private JpaQuestionRepository questionRepository;

    @Mock
    private JpaAnswerRepository answerRepository;

    @Mock
    private FollowMapper followMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateFollowWhenFollowDoesNotExistForQuestion() {
        Long userId = 1L;
        Long questionId = 2L;
        JpaUser jpaUser = new JpaUser();
        JpaQuestion jpaQuestion = new JpaQuestion();
        JpaFollow jpaFollow = new JpaFollow();
        FollowResponse followResponse = new FollowResponse();
        followResponse.setId(1L);

        when(followRepository.findByUserIdAndQuestionId(userId, questionId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(jpaUser));
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(jpaQuestion));
        when(followRepository.save(any(JpaFollow.class))).thenReturn(jpaFollow);
        when(followMapper.toResponse(any(JpaFollow.class))).thenReturn(followResponse);

        FollowResponse result = followService.toggleFollowQuestion(new FollowQuestionRequest(userId, questionId));

        assertNotNull(result);
        verify(followRepository).save(any(JpaFollow.class));
        verify(followMapper).toResponse(any(JpaFollow.class));
    }

    @Test
    void shouldDeleteFollowWhenFollowExistsForQuestion() {
        Long userId = 1L;
        Long questionId = 2L;
        JpaFollow existingFollow = new JpaFollow();

        when(followRepository.findByUserIdAndQuestionId(userId, questionId))
                .thenReturn(Optional.of(existingFollow));

        FollowResponse result = followService.toggleFollowQuestion(new FollowQuestionRequest(userId, questionId));

        assertNull(result);
        verify(followRepository).delete(existingFollow);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForFollowQuestiond() {
        Long userId = 1L;
        Long questionId = 2L;

        when(followRepository.findByUserIdAndQuestionId(userId, questionId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        FollowQuestionRequest request = new FollowQuestionRequest(userId, questionId);
        assertThrows(FilterFollowQuestionExeption.class, () -> followService.toggleFollowQuestion(request));
    }

    @Test
    void shouldCreateFollowWhenFollowDoesNotExistForAnswer() {
        Long userId = 1L;
        Long answerId = 2L;
        JpaUser jpaUser = new JpaUser();
        JpaAnswer jpaAnswer = new JpaAnswer();
        JpaFollow jpaFollow = new JpaFollow();
        FollowResponse followResponse = new FollowResponse();
        followResponse.setId(1L);

        when(followRepository.findByUserIdAndAnswerId(userId, answerId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(jpaUser));
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(jpaAnswer));
        when(followRepository.save(any(JpaFollow.class))).thenReturn(jpaFollow);
        when(followMapper.toResponse(any(JpaFollow.class))).thenReturn(followResponse);

        FollowResponse result = followService.toggleFollowAnswer(new FollowAnswerRequest(userId, answerId));

        assertNotNull(result);
        verify(followRepository).save(any(JpaFollow.class));
    }

    @Test
    void shouldDeleteFollowWhenFollowExistsForAnswer() {
        Long userId = 1L;
        Long answerId = 2L;
        JpaFollow existingFollow = new JpaFollow();

        when(followRepository.findByUserIdAndAnswerId(userId, answerId))
                .thenReturn(Optional.of(existingFollow));

        FollowResponse result = followService.toggleFollowAnswer(new FollowAnswerRequest(userId, answerId));

        assertNull(result);
        verify(followRepository).delete(existingFollow);
    }

    @Test
    void shouldMuteNotificationsWhenFollowExists() {
        Long followId = 1L;
        JpaFollow jpaFollow = new JpaFollow();
        when(followRepository.findById(followId)).thenReturn(Optional.of(jpaFollow));
        when(followRepository.save(any(JpaFollow.class))).thenReturn(jpaFollow);

        boolean result = followService.muteNotifications(followId);

        assertTrue(result);
        assertTrue(jpaFollow.isMuted());
        verify(followRepository).save(jpaFollow);
    }

    @Test
    void shouldReturnUserFollowsWhenUserHasFollows() {
        Long userId = 1L;
        List<JpaFollow> jpaFollows = Arrays.asList(new JpaFollow(), new JpaFollow());

        when(followRepository.findByUserId(userId)).thenReturn(jpaFollows);
        when(followMapper.toResponse(any(JpaFollow.class))).thenReturn(new FollowResponse());

        List<FollowResponse> result = followService.getUserFollows(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(followMapper, times(2)).toResponse(any(JpaFollow.class));
    }

    @Test
    void shouldReturnFollowedQuestionsWhenUserHasFollowedQuestions() {
        Long userId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);
        FollowedQuestionRequest request = new FollowedQuestionRequest(userId, pageable);
        Page<JpaFollow> followPage = new PageImpl<>(Arrays.asList(new JpaFollow(), new JpaFollow()));

        when(followRepository.findByUserIdAndJpaQuestionIsNotNull(userId, pageable)).thenReturn(followPage);
        when(followMapper.toResponse(any(JpaFollow.class))).thenReturn(new FollowResponse());

        Page<FollowResponse> result = followService.getFollowedQuestions(request);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    void shouldBatchMuteFollowsWhenFollowIdsAreProvided() {
        List<Long> followIds = Arrays.asList(1L, 2L);
        List<JpaFollow> follows = Arrays.asList(new JpaFollow(), new JpaFollow());

        when(followRepository.findAllById(followIds)).thenReturn(follows);
        when(followRepository.saveAll(any())).thenReturn(follows);

        boolean result = followService.batchMuteFollows(followIds);

        assertTrue(result);
        verify(followRepository).saveAll(follows);
        follows.forEach(follow -> assertTrue(follow.isMuted()));
    }

    @Test
    void shouldDeleteFollowsWhenBatchUnfollowIsCalled() {
        List<Long> followIds = Arrays.asList(1L, 2L);
        List<JpaFollow> follows = Arrays.asList(new JpaFollow(), new JpaFollow());

        when(followRepository.findAllById(followIds)).thenReturn(follows);

        boolean result = followService.batchUnfollow(followIds);

        assertTrue(result);
        verify(followRepository).deleteAll(follows);
    }

    @Test
    void shouldReturnEmptyPageWhenNoFollowedQuestions() {
        FollowedQuestionRequest request = new FollowedQuestionRequest(1L, Pageable.unpaged());

        when(followRepository.findByUserIdAndJpaQuestionIsNotNull(request.getUserId(), request.getPageable()))
                .thenReturn(Page.empty());

        Page<FollowResponse> result = followService.getFollowedQuestions(request);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyPageWhenNoFollowedAnswers() {
        FollowedAnswerRequest request = new FollowedAnswerRequest(1L, Pageable.unpaged());

        when(followRepository.findByUserIdAndJpaAnswerIsNotNull(request.getUserId(), request.getPageable()))
                .thenReturn(Page.empty());

        Page<FollowResponse> result = followService.getFollowedAnswers(request);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenNoUserFollows() {
        Long userId = 1L;

        when(followRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        List<FollowResponse> result = followService.getUserFollows(userId);

        assertTrue(result.isEmpty());
    }


    @Test
    void shouldReturnFollowedQuestions() {
        FollowedQuestionRequest request = new FollowedQuestionRequest(1L, Pageable.unpaged());
        Page<JpaFollow> jpaFollowPage = new PageImpl<>(List.of(new JpaFollow()));

        when(followRepository.findByUserIdAndJpaQuestionIsNotNull(request.getUserId(), request.getPageable()))
                .thenReturn(jpaFollowPage);

        Page<FollowResponse> result = followService.getFollowedQuestions(request);

        assertFalse(result.isEmpty());
    }

    @Test
    void shouldReturnFollowedAnswers() {
        FollowedAnswerRequest request = new FollowedAnswerRequest(1L, Pageable.unpaged());
        Page<JpaFollow> jpaFollowPage = new PageImpl<>(List.of(new JpaFollow()));

        when(followRepository.findByUserIdAndJpaAnswerIsNotNull(request.getUserId(), request.getPageable()))
                .thenReturn(jpaFollowPage);

        Page<FollowResponse> result = followService.getFollowedAnswers(request);

        assertFalse(result.isEmpty());
    }

    @Test
    void shouldThrowFilterFollowAnswerExceptionWhenErrorOccurs() {

        Long userId = 1L;
        Long answerId = 2L;
        FollowAnswerRequest request = new FollowAnswerRequest(userId, answerId);

        when(followRepository.findByUserIdAndAnswerId(userId, answerId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenThrow(new RuntimeException("User not found"));

        assertThrows(FilterFollowAnswerExeption.class, () -> followService.toggleFollowAnswer(request));

        verify(followRepository, never()).save(any(JpaFollow.class));
    }

    @Test
    void shouldThrowFilterFollowAnswerExceptionWhenAnswerNotFound() {

        Long userId = 1L;
        Long answerId = 2L;
        FollowAnswerRequest request = new FollowAnswerRequest(userId, answerId);

        when(followRepository.findByUserIdAndAnswerId(userId, answerId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(new JpaUser()));
        when(answerRepository.findById(answerId)).thenThrow(new RuntimeException("Answer not found"));

        assertThrows(FilterFollowAnswerExeption.class, () -> followService.toggleFollowAnswer(request));

        verify(followRepository, never()).save(any(JpaFollow.class));
    }

}
