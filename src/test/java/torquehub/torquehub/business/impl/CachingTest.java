package torquehub.torquehub.business.impl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.persistence.jpa.impl.JpaQuestionRepository;

import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CachingTest {

    @Autowired
    private QuestionServiceImpl questionService;

    @MockBean
    private JpaQuestionRepository questionRepository;

    @BeforeEach
    void setUp() {
        JpaQuestion question = new JpaQuestion();
        question.setId(1L);
        Mockito.when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
    }

    @Test
    @Transactional
    void testQuestionCache() {
        Long questionId = 1L;

        // First call: repository method should be invoked
        questionService.getQuestionbyId(questionId, Pageable.unpaged());
        Mockito.verify(questionRepository, Mockito.times(1)).findById(questionId);

        // Second call: cache should be hit, and repository method should NOT be invoked
        questionService.getQuestionbyId(questionId, Pageable.unpaged());
        Mockito.verify(questionRepository, Mockito.times(1)).findById(questionId); // Verify no additional call
    }
}
