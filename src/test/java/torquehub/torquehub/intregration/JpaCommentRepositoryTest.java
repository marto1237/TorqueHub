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
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.model.jpa_models.JpaComment;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.persistence.jpa.impl.JpaCommentRepository;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaCommentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaCommentRepository.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaCommentRepositoryTest {

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
    private SpringDataJpaCommentRepository springDataJpaCommentRepository;

    @Autowired
    private JpaCommentRepository commentRepository;

    @Autowired
    private EntityManager entityManager;

    private JpaUser user;
    private JpaQuestion question;
    private JpaAnswer answer;

    @BeforeEach
    void setUp() {
        springDataJpaCommentRepository.deleteAll();

        // Create and persist a role
        JpaRole role = JpaRole.builder()
                .name("USER_ROLE")
                .build();
        entityManager.persist(role);

        // Create and persist a user
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

        // Create and persist a question
        question = JpaQuestion.builder()
                .title("Sample Question")
                .description("This is a sample question")
                .jpaUser(user)
                .askedTime(LocalDateTime.now())
                .lastActivityTime(LocalDateTime.now())
                .build();
        entityManager.persist(question);

        // Create and persist an answer
        answer = JpaAnswer.builder()
                .text("Sample Answer")
                .jpaUser(user)
                .jpaQuestion(question)
                .answeredTime(LocalDateTime.now())
                .build();
        entityManager.persist(answer);
    }

    @Test
    @DisplayName("Save a new comment successfully")
    void testSaveNewComment() {
        // Arrange
        JpaComment comment = JpaComment.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .text("This is a test comment")
                .votes(0)
                .commentedTime(LocalDateTime.now())
                .isEdited(false)
                .build();

        // Act
        JpaComment savedComment = commentRepository.save(comment);

        // Assert
        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getJpaUser().getId()).isEqualTo(user.getId());
        assertThat(savedComment.getJpaAnswer().getId()).isEqualTo(answer.getId());
        assertThat(savedComment.getText()).isEqualTo("This is a test comment");
    }

    @Test
    @DisplayName("Find comment by ID")
    void testFindById() {
        // Arrange
        JpaComment comment = JpaComment.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .text("Find by ID Comment")
                .votes(5)
                .commentedTime(LocalDateTime.now())
                .isEdited(false)
                .build();
        JpaComment savedComment = commentRepository.save(comment);

        // Act
        Optional<JpaComment> foundComment = commentRepository.findById(savedComment.getId());

        // Assert
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getText()).isEqualTo("Find by ID Comment");
    }

    @Test
    @DisplayName("Find comments by answer ID")
    void testFindByAnswerId() {
        // Arrange
        JpaComment comment1 = JpaComment.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .text("Answer Comment 1")
                .votes(2)
                .commentedTime(LocalDateTime.now())
                .isEdited(false)
                .build();
        JpaComment comment2 = JpaComment.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .text("Answer Comment 2")
                .votes(1)
                .commentedTime(LocalDateTime.now())
                .isEdited(true)
                .build();
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        // Act
        List<JpaComment> comments = commentRepository.findByAnswerId(answer.getId());

        // Assert
        assertThat(comments).hasSize(2);
    }

    @Test
    @DisplayName("Find comments by user ID")
    void testFindByUserId() {
        // Arrange
        JpaComment comment = JpaComment.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .text("User Comment")
                .votes(3)
                .commentedTime(LocalDateTime.now())
                .isEdited(false)
                .build();
        commentRepository.save(comment);

        // Act
        List<JpaComment> comments = commentRepository.findByUserId(user.getId());

        // Assert
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getText()).isEqualTo("User Comment");
    }

    @Test
    @DisplayName("Delete comment by ID")
    void testDeleteById() {
        // Arrange
        JpaComment comment = JpaComment.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .text("Delete Comment")
                .votes(0)
                .commentedTime(LocalDateTime.now())
                .isEdited(false)
                .build();
        JpaComment savedComment = commentRepository.save(comment);

        // Act
        boolean deleted = commentRepository.deleteById(savedComment.getId());

        // Assert
        assertThat(deleted).isTrue();
        assertThat(commentRepository.findById(savedComment.getId())).isEmpty();
    }

    @Test
    @DisplayName("Find paginated comments by answer ID")
    void testFindByAnswerIdPaginated() {
        // Arrange
        JpaComment comment = JpaComment.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .text("Paginated Comment")
                .votes(1)
                .commentedTime(LocalDateTime.now())
                .isEdited(false)
                .build();
        commentRepository.save(comment);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<JpaComment> commentsPage = commentRepository.findByAnswerId(answer.getId(), pageable);

        // Assert
        assertThat(commentsPage.getTotalElements()).isEqualTo(1);
    }
}
