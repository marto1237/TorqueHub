package torquehub.torquehub.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import torquehub.torquehub.domain.mapper.AnswerMapper;
import torquehub.torquehub.domain.mapper.CommentMapper;
import torquehub.torquehub.domain.model.jpa_models.*;
import torquehub.torquehub.domain.request.answer_dtos.AnswerCreateRequest;
import torquehub.torquehub.domain.request.answer_dtos.AnswerEditRequest;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.comment_dtos.CommentResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaBookmarkRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaFollowRepository;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerMapperTest {

    private AnswerMapper answerMapper = Mappers.getMapper(AnswerMapper.class);

    @Mock
    private JpaBookmarkRepository bookmarkRepository;

    @Mock
    private JpaFollowRepository followRepository;

    @Mock
    private CommentMapper commentMapper;

    private JpaAnswer jpaAnswer;
    private JpaUser jpaUser;
    private JpaComment jpaComment;

    @BeforeEach
    void setUp() {
        jpaUser = new JpaUser();
        jpaUser.setUsername("testUser");
        jpaUser.setPoints(100);

        jpaComment = new JpaComment();
        jpaComment.setText("Sample Comment");

        jpaAnswer = new JpaAnswer();
        jpaAnswer.setId(1L);
        jpaAnswer.setJpaUser(jpaUser);
        jpaAnswer.setEdited(false);
        jpaAnswer.setAnsweredTime(LocalDateTime.now());
        jpaAnswer.setJpaComments(List.of(jpaComment));
    }


    @Test
    void testToEntityFromCreateRequest() {
        AnswerCreateRequest createRequest = new AnswerCreateRequest();
        createRequest.setText("Test Content");

        JpaAnswer result = answerMapper.toEntity(createRequest);

        assertEquals("Test Content", result.getText());
    }

    @Test
    void testToEntityFromEditRequest() {
        AnswerEditRequest editRequest = new AnswerEditRequest();
        editRequest.setText("Edited Content");

        JpaAnswer result = answerMapper.toEntity(editRequest);

        assertEquals("Edited Content", result.getText());
    }

    @Test
    void testToResponseWithCommentsAndUserDetails() {
        when(bookmarkRepository.findByUserIdAndAnswerId(anyLong(), anyLong())).thenReturn(Optional.of(new JpaBookmark()));
        when(followRepository.findByUserIdAndAnswerId(anyLong(), anyLong())).thenReturn(Optional.of(new JpaFollow()));

        // Mock commentMapper response
        CommentResponse mockCommentResponse = new CommentResponse();
        mockCommentResponse.setText("Sample Comment");
        when(commentMapper.toResponse(any(JpaComment.class))).thenReturn(mockCommentResponse);

        AnswerResponse response = answerMapper.toResponse(jpaAnswer, 1L, bookmarkRepository, followRepository, commentMapper);

        assertNotNull(response);
        assertEquals("testUser", response.getUsername());
        assertEquals(100, response.getUserPoints());
        assertFalse(response.isEdited());
        assertEquals(1, response.getComments().size());
        assertEquals("Sample Comment", response.getComments().get(0).getText());
    }

    @Test
    void testToResponseWithNoComments() {
        jpaAnswer.setJpaComments(List.of());

        AnswerResponse response = answerMapper.toResponse(jpaAnswer, 1L, bookmarkRepository, followRepository, commentMapper);

        assertNotNull(response);
        assertTrue(response.getComments().isEmpty());
    }

    @Test
    void testToResponseWhenUserIsNotFollowingOrBookmarked() {
        when(bookmarkRepository.findByUserIdAndAnswerId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(followRepository.findByUserIdAndAnswerId(anyLong(), anyLong())).thenReturn(Optional.empty());

        AnswerResponse response = answerMapper.toResponse(jpaAnswer, 1L, bookmarkRepository, followRepository, commentMapper);

        assertNotNull(response);
    }

    @Test
    void testToResponseWhenUserIdIsNull() {
        AnswerResponse response = answerMapper.toResponse(jpaAnswer, null, bookmarkRepository, followRepository, commentMapper);

        assertNotNull(response);
    }

    @Test
    void testLimitComments() {
        List<JpaComment> jpaComments = List.of(
                new JpaComment(), new JpaComment(), new JpaComment(),
                new JpaComment(), new JpaComment(), new JpaComment()
        );

        CommentResponse mockCommentResponse = CommentResponse.builder()
                .text("Sample Comment")
                .build();

        when(commentMapper.toResponse(any(JpaComment.class))).thenReturn(mockCommentResponse);

        List<CommentResponse> limitedComments = answerMapper.limitComments(jpaComments, 0, commentMapper);

        assertEquals(5, limitedComments.size()); // Limited to 5 comments
    }

    @Test
    void testIsBookmarked() {
        when(bookmarkRepository.findByUserIdAndAnswerId(1L, 1L)).thenReturn(Optional.of(new JpaBookmark()));

        assertTrue(answerMapper.isBookmarked(1L, 1L, bookmarkRepository));
    }

    @Test
    void testIsBookmarkedWhenNotBookmarked() {
        when(bookmarkRepository.findByUserIdAndAnswerId(1L, 1L)).thenReturn(Optional.empty());

        assertFalse(answerMapper.isBookmarked(1L, 1L, bookmarkRepository));
    }

    @Test
    void testIsFollowing() {
        when(followRepository.findByUserIdAndAnswerId(1L, 1L)).thenReturn(Optional.of(new JpaFollow()));

        assertTrue(answerMapper.isFollowing(1L, 1L, followRepository));
    }

    @Test
    void testIsFollowingWhenNotFollowing() {
        when(followRepository.findByUserIdAndAnswerId(1L, 1L)).thenReturn(Optional.empty());

        assertFalse(answerMapper.isFollowing(1L, 1L, followRepository));
    }

}
