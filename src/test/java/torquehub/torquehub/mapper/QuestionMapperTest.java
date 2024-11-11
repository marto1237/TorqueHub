package torquehub.torquehub.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import torquehub.torquehub.domain.mapper.AnswerMapper;
import torquehub.torquehub.domain.mapper.CommentMapper;
import torquehub.torquehub.domain.mapper.QuestionMapper;
import torquehub.torquehub.domain.mapper.QuestionMapperContext;
import torquehub.torquehub.domain.model.jpa_models.*;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionDetailResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaBookmarkRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaFollowRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaVoteRepository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionMapperTest {

    private final QuestionMapper mapper = Mappers.getMapper(QuestionMapper.class);

    @Mock
    private AnswerMapper answerMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private JpaBookmarkRepository bookmarkRepository;

    @Mock
    private JpaFollowRepository followRepository;

    @Mock
    private JpaVoteRepository voteRepository;

    private JpaQuestion jpaQuestion;
    private JpaUser jpaUser;
    private JpaAnswer jpaAnswer;
    private JpaTag jpaTag;
    private QuestionMapperContext context;

    @BeforeEach
    void setUp() {
        jpaUser = new JpaUser();
        jpaUser.setUsername("testUser");
        jpaUser.setPoints(100);

        jpaTag = new JpaTag();
        jpaTag.setId(1L);
        jpaTag.setName("BMW");

        jpaAnswer = new JpaAnswer();
        jpaAnswer.setId(1L);
        jpaAnswer.setJpaUser(jpaUser);

        jpaQuestion = new JpaQuestion();
        jpaQuestion.setId(1L);
        jpaQuestion.setJpaUser(jpaUser);
        jpaQuestion.setJpaTags(Set.of(jpaTag)); // Ensure tags are initialized
        jpaQuestion.setJpaAnswers(List.of(jpaAnswer));

        context = new QuestionMapperContext(commentMapper, answerMapper, bookmarkRepository, followRepository, voteRepository, 1L, PageRequest.of(0, 5));
    }


    @Test
    void toResponse_ShouldMapQuestionToResponse() {
        QuestionResponse response = mapper.toResponse(jpaQuestion);

        assertEquals(Set.of("BMW"), response.getTags());
        assertEquals("testUser", response.getUsername());
    }

    @Test
    void toSummaryResponse_ShouldMapQuestionToSummaryResponse() {
        QuestionSummaryResponse response = mapper.toSummaryResponse(jpaQuestion);

        assertEquals(Set.of("BMW"), response.getTags());
        assertEquals("testUser", response.getUserName());
        assertEquals(100, response.getUserPoints());
        assertEquals(1, response.getTotalAnswers());
    }

    @Test
    void toDetailResponse_ShouldMapQuestionToDetailResponse() {
        AnswerResponse mockAnswerResponse = new AnswerResponse();
        mockAnswerResponse.setId(1L);
        when(answerMapper.toResponse(any(JpaAnswer.class), anyLong(), any(), any(), any(), any())).thenReturn(mockAnswerResponse);

        QuestionDetailResponse response = mapper.toDetailResponse(jpaQuestion, context);

        assertEquals("testUser", response.getUserName());
        assertEquals(100, response.getUserPoints());
        assertEquals(Set.of("BMW"), response.getTags());
        assertEquals(1, response.getAnswers().getTotalElements());
    }

    @Test
    void mapTagsToTagNames_ShouldReturnCorrectTagNames() {
        Set<String> tagNames = mapper.mapTagsToTagNames(Set.of(jpaTag));

        assertEquals(Set.of("BMW"), tagNames);
    }

    @Test
    void mapTagNamesToTags_ShouldReturnCorrectTags() {
        Set<JpaTag> tags = mapper.mapTagNamesToTags(Set.of("BMW"));

        assertEquals(1, tags.size());
        assertTrue(tags.stream().anyMatch(tag -> tag.getName().equals("BMW")));
    }

    @Test
    void mapAnswersToPagedAnswerResponses_ShouldReturnPagedAnswerResponses() {
        AnswerResponse mockAnswerResponse = new AnswerResponse();
        mockAnswerResponse.setId(1L);
        when(answerMapper.toResponse(any(JpaAnswer.class), anyLong(), any(), any(), any(), any())).thenReturn(mockAnswerResponse);

        Page<AnswerResponse> responsePage = mapper.mapAnswersToPagedAnswerResponses(List.of(jpaAnswer), context);

        assertEquals(1, responsePage.getTotalElements());
        assertEquals(1L, responsePage.getContent().get(0).getId());
    }

    @Test
    void mapAnswersToPagedAnswerResponses_ShouldHandleEmptyAnswers() {
        Page<AnswerResponse> responsePage = mapper.mapAnswersToPagedAnswerResponses(Collections.emptyList(), context);

        assertEquals(0, responsePage.getTotalElements());
    }
}

