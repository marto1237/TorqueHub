package torquehub.torquehub.intregration;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.persistence.jpa.impl.JpaQuestionRepository;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaQuestionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaQuestionRepository.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaQuestionRepositoryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpassword")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
    }

    @Autowired
    private SpringDataJpaQuestionRepository springDataJpaQuestionRepository;

    @Autowired
    private JpaQuestionRepository questionRepository;

    @Autowired
    private EntityManager entityManager;

    private JpaUser user;
    private JpaTag tag;

    @BeforeEach
    void setUp() {
        springDataJpaQuestionRepository.deleteAll();

        JpaRole role = JpaRole.builder()
                .name("USER_ROLE")
                .build();
        entityManager.persist(role);

        user = JpaUser.builder()
                .username("testuser")
                .email("testuser@example.com")
                .jpaRole(role)
                .password("password")
                .salt("randomSalt")
                .points(10)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(user);

        tag = JpaTag.builder()
                .name("Sample Tag")
                .build();
        entityManager.persist(tag);
    }

    @Test
    @DisplayName("Save a new question successfully")
    void testSaveNewQuestion() {
        // Arrange
        JpaQuestion question = JpaQuestion.builder()
                .title("Sample Question")
                .description("This is a sample question description.")
                .jpaUser(user)
                .jpaTags(Set.of(tag))
                .askedTime(LocalDateTime.now())
                .lastActivityTime(LocalDateTime.now())
                .build();

        // Act
        JpaQuestion savedQuestion = questionRepository.save(question);

        // Assert
        assertThat(savedQuestion.getId()).isNotNull();
        assertThat(savedQuestion.getJpaUser().getId()).isEqualTo(user.getId());
        assertThat(savedQuestion.getTitle()).isEqualTo("Sample Question");
    }

    @Test
    @DisplayName("Find question by ID")
    void testFindById() {
        // Arrange
        JpaQuestion question = JpaQuestion.builder()
                .title("Question to Find")
                .description("This is a sample description.")
                .jpaUser(user)
                .askedTime(LocalDateTime.now())
                .lastActivityTime(LocalDateTime.now())
                .build();
        JpaQuestion savedQuestion = questionRepository.save(question);

        // Act
        Optional<JpaQuestion> foundQuestion = questionRepository.findById(savedQuestion.getId());

        // Assert
        assertThat(foundQuestion).isPresent();
        assertThat(foundQuestion.get().getTitle()).isEqualTo("Question to Find");
    }

    @Test
    @DisplayName("Delete question by ID")
    void testDeleteById() {
        // Arrange
        JpaQuestion question = JpaQuestion.builder()
                .title("Question to Delete")
                .description("This is a sample description.")
                .jpaUser(user)
                .askedTime(LocalDateTime.now())
                .lastActivityTime(LocalDateTime.now())
                .build();
        JpaQuestion savedQuestion = questionRepository.save(question);

        // Act
        boolean isDeleted = questionRepository.deleteById(savedQuestion.getId());

        // Assert
        assertThat(isDeleted).isTrue();
        assertThat(questionRepository.findById(savedQuestion.getId())).isEmpty();
    }

    @Test
    @DisplayName("Find questions by tags")
    void testFindQuestionsByTags() {
        // Arrange
        JpaQuestion question = JpaQuestion.builder()
                .title("Tagged Question")
                .description("This is a tagged question.")
                .jpaUser(user)
                .jpaTags(Set.of(tag))
                .askedTime(LocalDateTime.now())
                .lastActivityTime(LocalDateTime.now())
                .build();
        questionRepository.save(question);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<JpaQuestion> taggedQuestions = questionRepository.findQuestionsByTags(List.of(tag), pageable);

        // Assert
        assertThat(taggedQuestions.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Find questions ordered by asked time")
    void testFindAllByOrderByAskedTimeDesc() {
        // Arrange
        JpaQuestion question = JpaQuestion.builder()
                .title("Recent Question")
                .description("This is a recent question.")
                .jpaUser(user)
                .askedTime(LocalDateTime.now())
                .lastActivityTime(LocalDateTime.now())
                .build();
        questionRepository.save(question);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<JpaQuestion> recentQuestions = questionRepository.findAllByOrderByAskedTimeDesc(pageable);

        // Assert
        assertThat(recentQuestions.getTotalElements()).isEqualTo(1);
        assertThat(recentQuestions.getContent().get(0).getTitle()).isEqualTo("Recent Question");
    }

    @Test
    @DisplayName("Find questions ordered by votes")
    void testFindAllByOrderByVotesDesc() {
        // Arrange
        JpaQuestion question = JpaQuestion.builder()
                .title("Popular Question")
                .description("This is a popular question.")
                .votes(5)
                .jpaUser(user)
                .askedTime(LocalDateTime.now())
                .lastActivityTime(LocalDateTime.now())
                .build();
        questionRepository.save(question);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<JpaQuestion> popularQuestions = questionRepository.findAllByOrderByVotesDesc(pageable);

        // Assert
        assertThat(popularQuestions.getTotalElements()).isEqualTo(1);
        assertThat(popularQuestions.getContent().get(0).getVotes()).isEqualTo(5);
    }

    @Test
    @DisplayName("Find questions with no answers")
    void testFindQuestionsWithNoAnswers() {
        // Arrange
        JpaQuestion question = JpaQuestion.builder()
                .title("Unanswered Question")
                .description("This is a question with no answers.")
                .jpaUser(user)
                .askedTime(LocalDateTime.now())
                .lastActivityTime(LocalDateTime.now())
                .build();
        questionRepository.save(question);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<JpaQuestion> unansweredQuestions = questionRepository.findQuestionsWithNoAnswers(pageable);

        // Assert
        assertThat(unansweredQuestions.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Count questions by user ID")
    void testCountByUserId() {
        // Arrange
        JpaQuestion question1 = JpaQuestion.builder()
                .title("Question 1")
                .description("Description 1")
                .jpaUser(user)
                .askedTime(LocalDateTime.now())
                .lastActivityTime(LocalDateTime.now())
                .build();
        JpaQuestion question2 = JpaQuestion.builder()
                .title("Question 2")
                .description("Description 2")
                .jpaUser(user)
                .askedTime(LocalDateTime.now())
                .lastActivityTime(LocalDateTime.now())
                .build();
        questionRepository.save(question1);
        questionRepository.save(question2);

        // Act
        Long count = questionRepository.countByJpaUserId(user.getId());

        // Assert
        assertThat(count).isEqualTo(2);
    }

}