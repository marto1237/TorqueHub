package torquehub.torquehub.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import torquehub.torquehub.domain.mapper.CommentMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaComment;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.model.jpa_models.JpaVote;
import torquehub.torquehub.domain.request.comment_dtos.CommentCreateRequest;
import torquehub.torquehub.domain.request.comment_dtos.CommentEditRequest;
import torquehub.torquehub.domain.response.comment_dtos.CommentResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaVoteRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentMapperTest {

    private CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

    @Mock
    private JpaVoteRepository voteRepository;

    private JpaComment jpaComment;
    private JpaUser jpaUser;

    @BeforeEach
    void setUp() {
        jpaUser = new JpaUser();
        jpaUser.setUsername("testUser");
        jpaUser.setPoints(100);

        jpaComment = new JpaComment();
        jpaComment.setId(1L);
        jpaComment.setJpaUser(jpaUser);
        jpaComment.setText("Sample Comment");
        jpaComment.setCommentedTime(LocalDateTime.now());
    }

    @Test
    void testToEntityFromCreateRequest() {
        CommentCreateRequest createRequest = new CommentCreateRequest();
        createRequest.setText("Test Content");

        JpaComment result = commentMapper.toEntity(createRequest);

        assertEquals("Test Content", result.getText());
    }

    @Test
    void testToEntityFromEditRequest() {
        CommentEditRequest editRequest = new CommentEditRequest();
        editRequest.setText("Edited Content");

        JpaComment result = commentMapper.toEntity(editRequest);

        assertEquals("Edited Content", result.getText());
    }

    @Test
    void testToResponseWithUserDetailsAndUpvote() {
        // Mock user upvote
        JpaVote upvote = new JpaVote();
        upvote.setUpvote(true);
        when(voteRepository.findByUserIdAndCommentId(anyLong(), anyLong())).thenReturn(Optional.of(upvote));

        CommentResponse response = commentMapper.toResponse(jpaComment, 1L, voteRepository);

        assertNotNull(response);
        assertEquals("testUser", response.getUsername());
        assertEquals(100, response.getUserPoints());
        assertEquals("up", response.getUserVote());
        assertEquals("Sample Comment", response.getText());
    }

    @Test
    void testToResponseWithUserDetailsAndDownvote() {
        // Mock user downvote
        JpaVote downvote = new JpaVote();
        downvote.setUpvote(false);
        when(voteRepository.findByUserIdAndCommentId(anyLong(), anyLong())).thenReturn(Optional.of(downvote));

        CommentResponse response = commentMapper.toResponse(jpaComment, 1L, voteRepository);

        assertNotNull(response);
        assertEquals("down", response.getUserVote());
    }

    @Test
    void testToResponseWhenUserHasNotVoted() {
        // Mock no vote found for the user
        when(voteRepository.findByUserIdAndCommentId(anyLong(), anyLong())).thenReturn(Optional.empty());

        CommentResponse response = commentMapper.toResponse(jpaComment, 1L, voteRepository);

        assertNotNull(response);
        assertNull(response.getUserVote());
    }

    @Test
    void testToResponseWhenUserIdIsNull() {
        // Ensure no exception when userId is null
        CommentResponse response = commentMapper.toResponse(jpaComment, null, voteRepository);

        assertNotNull(response);
        assertNull(response.getUserVote());
    }

}
