package torquehub.torquehub.mapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import torquehub.torquehub.domain.mapper.QuestionMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;
import torquehub.torquehub.domain.response.question_dtos.QuestionResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestionMapperTest {
    private final QuestionMapper mapper = Mappers.getMapper(QuestionMapper.class);

    @Test
    void toResponse_ShouldMapQuestionToResponse() {
        JpaQuestion jpaQuestion = new JpaQuestion();
        jpaQuestion.setJpaTags(Set.of(new JpaTag(1L,"BMW")));

        QuestionResponse response = mapper.toResponse(jpaQuestion);

        assertEquals(Set.of("BMW"), response.getTags());
    }

    @Test
    void toSummaryResponse_ShouldMapQuestionToSummaryResponse() {
        JpaQuestion jpaQuestion = mock(JpaQuestion.class);
        when(jpaQuestion.getJpaTags()).thenReturn(Set.of(new JpaTag(1L,"BMW")));

        QuestionSummaryResponse response = mapper.toSummaryResponse(jpaQuestion);

        assertEquals(Set.of("BMW"), response.getTags());
    }
}

