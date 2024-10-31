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
import torquehub.torquehub.business.exeption.answer_exptions.AnswerNotFoundException;
import torquehub.torquehub.business.exeption.comment_exeptions.*;
import torquehub.torquehub.business.exeption.user_exeptions.UserNotFoundException;
import torquehub.torquehub.business.interfaces.NotificationService;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.business.interfaces.VoteService;
import torquehub.torquehub.domain.mapper.CommentMapper;
import torquehub.torquehub.domain.model.jpa_models.*;
import torquehub.torquehub.domain.request.comment_dtos.CommentCreateRequest;
import torquehub.torquehub.domain.request.comment_dtos.CommentEditRequest;
import torquehub.torquehub.domain.request.notification_dtos.CreateCommentAnswerRequest;
import torquehub.torquehub.domain.response.comment_dtos.CommentResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaAnswerRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaCommentRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaUserRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private JpaCommentRepository commentRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private JpaAnswerRepository answerRepository;

    @Mock
    private ReputationService reputationService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private VoteService voteService;

    @Mock
    private CommentMapper commentMapper;

    private JpaUser testUser;
    private JpaAnswer testAnswer;
    private JpaComment testComment;
    private CommentCreateRequest commentCreateRequest;
    private CommentEditRequest commentEditRequest;

    @BeforeEach
    void setUp() {
        testUser = JpaUser.builder().id(1L).username("testUser").build();
        testAnswer = JpaAnswer.builder().id(1L).jpaUser(testUser).jpaQuestion(new JpaQuestion()).build();
        testComment = JpaComment.builder().id(1L).jpaUser(testUser).jpaAnswer(testAnswer).text("Test Comment").build();

        commentCreateRequest = CommentCreateRequest.builder()
                .userId(1L)
                .answerId(1L)
                .text("New Comment")
                .build();

        commentEditRequest = CommentEditRequest.builder()
                .text("Edited Comment")
                .build();
    }

    @Test
    void shouldAddCommentSuccessfully_whenValidRequestProvided() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(answerRepository.findById(anyLong())).thenReturn(Optional.of(testAnswer));
        when(commentRepository.save(any(JpaComment.class))).thenReturn(testComment);
        when(reputationService.updateReputationForNewComment(any())).thenReturn(new ReputationResponse());
        when(commentMapper.toResponse(any(JpaComment.class))).thenReturn(new CommentResponse());

        CommentResponse response = commentService.addComment(commentCreateRequest);

        assertNotNull(response);
        verify(commentRepository).save(any(JpaComment.class));
        verify(reputationService).updateReputationForNewComment(any());
        verify(notificationService).notifyAnswerOwnerForNewComment(any(CreateCommentAnswerRequest.class));
    }

    @Test
    void shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> commentService.addComment(commentCreateRequest));
    }

    @Test
    void shouldThrowAnswerNotFoundException_whenAnswerDoesNotExist() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(answerRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(AnswerNotFoundException.class, () -> commentService.addComment(commentCreateRequest));
    }

    @Test
    void shouldEditCommentSuccessfully_whenCommentExists() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(JpaComment.class))).thenReturn(testComment);
        when(commentMapper.toResponse(any(JpaComment.class))).thenReturn(new CommentResponse());

        CommentResponse response = commentService.editComment(1L, commentEditRequest);

        assertNotNull(response);
        assertEquals("Edited Comment", testComment.getText());
        verify(commentRepository).save(any(JpaComment.class));
    }

    @Test
    void shouldThrowCommentNotFoundException_whenCommentDoesNotExist() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.editComment(1L, commentEditRequest));
    }

    @Test
    void shouldDeleteCommentSuccessfully_whenCommentExists() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(testComment));
        when(reputationService.updateReputationForCommentWhenCommentIsDeleted(any())).thenReturn(true);

        boolean result = commentService.deleteComment(1L);

        assertTrue(result);
        verify(commentRepository).deleteById(anyLong());
    }

    @Test
    void shouldThrowCommentDeleteException_whenReputationUpdateFails() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(testComment));
        when(reputationService.updateReputationForCommentWhenCommentIsDeleted(any())).thenReturn(false);

        assertThrows(CommentDeleteExeption.class, () -> commentService.deleteComment(1L));
    }

    @Test
    void shouldReturnCommentResponse_whenCommentExistsById() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(testComment));
        when(commentMapper.toResponse(any(JpaComment.class))).thenReturn(new CommentResponse());

        CommentResponse response = commentService.getCommentById(1L);

        assertNotNull(response);
    }

    @Test
    void shouldThrowCommentNotFoundException_whenCommentDoesNotExistById() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.getCommentById(1L));
    }

    @Test
    void shouldReturnCommentsByAnswer_whenCommentsExist() {
        when(commentRepository.findByAnswerId(anyLong())).thenReturn(Collections.singletonList(testComment));
        when(commentMapper.toResponse(any(JpaComment.class))).thenReturn(new CommentResponse());

        Optional<List<CommentResponse>> response = commentService.getCommentsByAnswer(1L);

        assertTrue(response.isPresent());
    }

    @Test
    void shouldReturnEmpty_whenNoCommentsExistByAnswer() {
        when(commentRepository.findByAnswerId(anyLong())).thenReturn(Collections.emptyList());

        Optional<List<CommentResponse>> response = commentService.getCommentsByAnswer(1L);

        assertTrue(response.isEmpty());
    }

    @Test
    void shouldUpvoteCommentSuccessfully() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(testComment));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(voteService.handleUpvoteForComment(any(JpaUser.class), any(JpaComment.class))).thenReturn(new ReputationResponse());

        ReputationResponse response = commentService.upvoteComment(1L, 1L);

        assertNotNull(response);
        verify(voteService).handleUpvoteForComment(any(JpaUser.class), any(JpaComment.class));
    }

    @Test
    void shouldDownvoteCommentSuccessfully() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(testComment));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(voteService.handleDownvoteForComment(any(JpaUser.class), any(JpaComment.class))).thenReturn(new ReputationResponse());

        ReputationResponse response = commentService.downvoteComment(1L, 1L);

        assertNotNull(response);
        verify(voteService).handleDownvoteForComment(any(JpaUser.class), any(JpaComment.class));
    }

    @Test
    void shouldReturnPaginatedComments_whenCalled() {
        Page<JpaComment> commentPage = new PageImpl<>(Collections.singletonList(testComment));
        when(commentRepository.findByAnswerId(anyLong(), any(Pageable.class))).thenReturn(commentPage);
        when(commentMapper.toResponse(any(JpaComment.class))).thenReturn(new CommentResponse());

        Page<CommentResponse> response = commentService.getPaginatedComments(1L, PageRequest.of(0, 10));

        assertFalse(response.isEmpty());
        verify(commentRepository).findByAnswerId(anyLong(), any(Pageable.class));
    }

    @Test
    void shouldThrowCommentDownvoteException_whenDownvoteFails() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(testComment));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(voteService.handleDownvoteForComment(any(JpaUser.class), any(JpaComment.class)))
                .thenThrow(new RuntimeException("Simulated failure"));  // Simulate a failure in vote handling

        assertThrows(CommentDownvoteException.class, () -> commentService.downvoteComment(1L, 1L));
    }

    @Test
    void shouldThrowCommentEditException_whenEditingFails() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(JpaComment.class)))
                .thenThrow(new IllegalArgumentException("Simulated failure"));

        assertThrows(CommentEditExeption.class, () -> commentService.editComment(1L, commentEditRequest));
    }

    @Test
    void shouldThrowCommentUpvoteException_whenUpvoteFails() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(testComment));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(voteService.handleUpvoteForComment(any(JpaUser.class), any(JpaComment.class)))
                .thenThrow(new RuntimeException("Simulated failure"));

        assertThrows(CommentUpvoteException.class, () -> commentService.upvoteComment(1L, 1L));
    }

    @Test
    void addComment_ShouldThrowAnswerNotFoundException_WhenAnswerDoesNotExist() {
        Long nonExistentAnswerId = 999L;
        Long userId = 1L;

        CommentCreateRequest commentRequest = CommentCreateRequest.builder()
                .userId(userId)
                .answerId(nonExistentAnswerId)
                .text("This is a test comment.")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(new JpaUser()));

        when(answerRepository.findById(nonExistentAnswerId)).thenReturn(Optional.empty());

        assertThrows(AnswerNotFoundException.class, () -> {
            commentService.addComment(commentRequest);
        });
    }

}
