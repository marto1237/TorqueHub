package torquehub.torquehub.intregration;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.persistence.jpa.impl.JpaAnswerRepository;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaAnswerRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaAnswerRepository.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaAnswerRepositoryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpassword")
            .withCommand("--character-set-server=utf8mb4",
                    "--collation-server=utf8mb4_unicode_ci",
                    "--skip-character-set-client-handshake");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
    }

    @Autowired
    private SpringDataJpaAnswerRepository springDataJpaAnswerRepository;

    @Autowired
    private JpaAnswerRepository answerRepository;

    @Autowired
    private EntityManager entityManager;

    private JpaUser user;
    private JpaQuestion question;

    @BeforeEach
    void setUp() {
        springDataJpaAnswerRepository.deleteAll();

        JpaRole role = JpaRole.builder()
                .name("USER_ROLE")
                .build();
        entityManager.persist(role);

        // Create a mock user
        user = JpaUser.builder()
                .username("testuser")
                .email("testuser@example.com")
                .jpaRole(role)
                .password("password")
                .salt("randomSalt")
                .points(10)
                .build();
        entityManager.persist(user);

        // Create a mock question
        question = JpaQuestion.builder()
                .title("Sample Question")
                .description("This is a sample question")
                .jpaUser(user)
                .askedTime(LocalDateTime.now())
                .lastActivityTime(LocalDateTime.now())
                .build();
        entityManager.persist(question);
    }

    @Test
    @DisplayName("Save a new answer successfully")
    void testSaveNewAnswer() {
        // Arrange
        JpaAnswer answer = JpaAnswer.builder()
                .text("Sample Answer")
                .jpaUser(user)
                .jpaQuestion(question)
                .answeredTime(LocalDateTime.now())
                .build();

        // Act
        JpaAnswer savedAnswer = answerRepository.save(answer);

        // Assert
        assertThat(savedAnswer.getId()).isNotNull();
        assertThat(savedAnswer.getText()).isEqualTo("Sample Answer");
        assertThat(savedAnswer.getJpaUser().getId()).isEqualTo(user.getId());
        assertThat(savedAnswer.getJpaQuestion().getId()).isEqualTo(question.getId());
    }

    @Test
    @DisplayName("Find answer by ID")
    void testFindById() {
        // Arrange
        JpaAnswer answer = JpaAnswer.builder()
                .text("Find by ID Answer")
                .jpaUser(user)
                .jpaQuestion(question)
                .answeredTime(LocalDateTime.now())
                .build();
        JpaAnswer savedAnswer = answerRepository.save(answer);

        // Act
        Optional<JpaAnswer> foundAnswer = answerRepository.findById(savedAnswer.getId());

        // Assert
        assertThat(foundAnswer).isPresent();
        assertThat(foundAnswer.get().getText()).isEqualTo("Find by ID Answer");
    }

    @Test
    @DisplayName("Find answers by user ID")
    void testFindByUserId() {
        // Arrange
        JpaAnswer answer1 = JpaAnswer.builder()
                .text("Answer 1")
                .jpaUser(user)
                .jpaQuestion(question)
                .answeredTime(LocalDateTime.now())
                .build();
        JpaAnswer answer2 = JpaAnswer.builder()
                .text("Answer 2")
                .jpaUser(user)
                .jpaQuestion(question)
                .answeredTime(LocalDateTime.now())
                .build();
        answerRepository.save(answer1);
        answerRepository.save(answer2);

        // Act
        List<JpaAnswer> userAnswers = answerRepository.findByUserId(user.getId());

        // Assert
        assertThat(userAnswers).hasSize(2);
        assertThat(userAnswers).extracting(JpaAnswer::getText)
                .containsExactlyInAnyOrder("Answer 1", "Answer 2");
    }

    @Test
    @DisplayName("Delete an existing answer by ID")
    void testDeleteById() {
        // Arrange
        JpaAnswer answer = JpaAnswer.builder()
                .text("Answer to delete")
                .jpaUser(user)
                .jpaQuestion(question)
                .answeredTime(LocalDateTime.now())
                .build();
        JpaAnswer savedAnswer = answerRepository.save(answer);

        // Act
        boolean deleted = answerRepository.deleteById(savedAnswer.getId());

        // Assert
        assertThat(deleted).isTrue();
        assertThat(answerRepository.findById(savedAnswer.getId())).isEmpty();
    }

    @Test
    @DisplayName("Find answers by question ID")
    void testFindByQuestionId() {
        // Arrange
        JpaAnswer answer1 = JpaAnswer.builder()
                .text("Answer for Question 1")
                .jpaUser(user)
                .jpaQuestion(question)
                .answeredTime(LocalDateTime.now())
                .build();
        answerRepository.save(answer1);

        // Act
        var answers = answerRepository.findByQuestionId(question.getId(), Pageable.unpaged());

        // Assert
        assertThat(answers).hasSize(1);
        assertThat(answers.getContent()).extracting(JpaAnswer::getText)
                .contains("Answer for Question 1");
    }
}
