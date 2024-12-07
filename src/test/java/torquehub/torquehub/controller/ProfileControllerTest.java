package torquehub.torquehub.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import torquehub.torquehub.business.interfaces.AnswerService;
import torquehub.torquehub.business.interfaces.QuestionService;
import torquehub.torquehub.business.interfaces.UserService;
import torquehub.torquehub.controllers.ProfileController;
import torquehub.torquehub.domain.mapper.ProfileMapper;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.profile_dtos.ProfileResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;
import torquehub.torquehub.domain.response.user_dtos.UserResponse;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileControllerTest {

    @InjectMocks
    private ProfileController profileController;

    @Mock
    private UserService userService;

    @Mock
    private QuestionService questionService;

    @Mock
    private AnswerService answerService;

    @Mock
    private ProfileMapper profileMapper;

    private Long userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = 1L;
    }

    @Test
    void getSiteData_Success() {
        // Arrange
        UserResponse mockUser = new UserResponse();
        List<QuestionSummaryResponse> mockQuestions = List.of(new QuestionSummaryResponse());
        List<AnswerResponse> mockAnswers = List.of(new AnswerResponse());
        Long questionCount = 10L;
        Long answerCount = 5L;

        ProfileResponse mockProfileResponse = new ProfileResponse();
        mockProfileResponse.setUser(mockUser);
        mockProfileResponse.setQuestions(mockQuestions);
        mockProfileResponse.setAnswers(mockAnswers);
        mockProfileResponse.setQuestionCount(questionCount);
        mockProfileResponse.setAnswerCount(answerCount);

        when(userService.getUserById(userId)).thenReturn(Optional.of(mockUser));
        when(questionService.getQuestionsByUser(userId)).thenReturn(Optional.of(mockQuestions));
        when(answerService.getAnswersByUser(userId)).thenReturn(Optional.of(mockAnswers));
        when(questionService.getQuestionCountOfUser(userId)).thenReturn(questionCount);
        when(answerService.getAnswerCountOfUser(userId)).thenReturn(answerCount);
        when(profileMapper.toProfileResponse(mockUser, mockQuestions, mockAnswers, questionCount, answerCount))
                .thenReturn(mockProfileResponse);

        // Act
        ResponseEntity<ProfileResponse> response = profileController.getSiteData(userId);

        // Assert
        assertNotNull(response);
        assertEquals(mockProfileResponse, response.getBody());
        verify(userService).getUserById(userId);
        verify(questionService).getQuestionsByUser(userId);
        verify(answerService).getAnswersByUser(userId);
    }

    @Test
    void getSiteData_UserNotFound() {
        when(userService.getUserById(userId)).thenReturn(Optional.empty());

        ResponseEntity<ProfileResponse> response = profileController.getSiteData(userId);

        assertNull(response.getBody());
    }
}
