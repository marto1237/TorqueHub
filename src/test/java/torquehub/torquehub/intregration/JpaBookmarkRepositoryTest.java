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
import torquehub.torquehub.domain.model.jpa_models.JpaBookmark;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.persistence.jpa.impl.JpaBookmarkRepository;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaBookmarkRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaBookmarkRepository.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaBookmarkRepositoryTest {

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
    private SpringDataJpaBookmarkRepository springDataJpaBookmarkRepository;

    @Autowired
    private JpaBookmarkRepository bookmarkRepository;

    @Autowired
    private EntityManager entityManager;

    private JpaUser user;
    private JpaQuestion question;
    private JpaAnswer answer;

    @BeforeEach
    void setUp() {
        springDataJpaBookmarkRepository.deleteAll();

        // Create and persist role
        JpaRole role = JpaRole.builder()
                .name("USER_ROLE")
                .build();
        entityManager.persist(role);

        // Create and persist user
        user = JpaUser.builder()
                .username("testuser")
                .email("testuser@example.com")
                .jpaRole(role)
                .password("password")
                .salt("randomSalt")
                .points(10)
                .build();
        entityManager.persist(user);

        // Create and persist question
        question = JpaQuestion.builder()
                .title("Sample Question")
                .description("This is a sample question")
                .jpaUser(user)
                .askedTime(LocalDateTime.now())
                .lastActivityTime(LocalDateTime.now())
                .build();
        entityManager.persist(question);

        // Create and persist answer
        answer = JpaAnswer.builder()
                .text("Sample Answer")
                .jpaUser(user)
                .jpaQuestion(question)
                .answeredTime(LocalDateTime.now())
                .build();
        entityManager.persist(answer);
    }

    @Test
    @DisplayName("Save a new bookmark successfully")
    void testSaveNewBookmark() {
        // Arrange
        JpaBookmark bookmark = JpaBookmark.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        JpaBookmark savedBookmark = bookmarkRepository.save(bookmark);

        // Assert
        assertThat(savedBookmark.getId()).isNotNull();
        assertThat(savedBookmark.getJpaUser().getId()).isEqualTo(user.getId());
        assertThat(savedBookmark.getJpaQuestion().getId()).isEqualTo(question.getId());
    }

    @Test
    @DisplayName("Find bookmark by ID")
    void testFindById() {
        // Arrange
        JpaBookmark bookmark = JpaBookmark.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .createdAt(LocalDateTime.now())
                .build();
        JpaBookmark savedBookmark = bookmarkRepository.save(bookmark);

        // Act
        Optional<JpaBookmark> foundBookmark = bookmarkRepository.findById(savedBookmark.getId());

        // Assert
        assertThat(foundBookmark).isPresent();
        assertThat(foundBookmark.get().getJpaQuestion().getId()).isEqualTo(question.getId());
    }

    @Test
    @DisplayName("Find bookmarks by user ID")
    void testFindByUserId() {
        // Arrange
        JpaBookmark bookmark1 = JpaBookmark.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .createdAt(LocalDateTime.now())
                .build();
        JpaBookmark bookmark2 = JpaBookmark.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .createdAt(LocalDateTime.now())
                .build();
        bookmarkRepository.save(bookmark1);
        bookmarkRepository.save(bookmark2);

        // Act
        List<JpaBookmark> userBookmarks = bookmarkRepository.findByUserId(user.getId());

        // Assert
        assertThat(userBookmarks).hasSize(2);
    }

    @Test
    @DisplayName("Find bookmark by user ID and question ID")
    void testFindByUserIdAndJpaQuestionId() {
        // Arrange
        JpaBookmark bookmark = JpaBookmark.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .createdAt(LocalDateTime.now())
                .build();
        bookmarkRepository.save(bookmark);

        // Act
        Optional<JpaBookmark> foundBookmark = bookmarkRepository.findByUserIdAndJpaQuestionId(user.getId(), question.getId());

        // Assert
        assertThat(foundBookmark).isPresent();
        assertThat(foundBookmark.get().getJpaQuestion().getId()).isEqualTo(question.getId());
    }

    @Test
    @DisplayName("Delete bookmark by ID")
    void testDeleteById() {
        // Arrange
        JpaBookmark bookmark = JpaBookmark.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .createdAt(LocalDateTime.now())
                .build();
        JpaBookmark savedBookmark = bookmarkRepository.save(bookmark);

        // Act
        bookmarkRepository.deleteById(savedBookmark.getId());

        // Assert
        assertThat(bookmarkRepository.findById(savedBookmark.getId())).isEmpty();
    }

    @Test
    @DisplayName("Find paginated bookmarks by user ID and non-null question")
    void testFindByUserIdAndJpaQuestionIsNotNull() {
        // Arrange
        JpaBookmark bookmark = JpaBookmark.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .createdAt(LocalDateTime.now())
                .build();
        bookmarkRepository.save(bookmark);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<JpaBookmark> bookmarksPage = bookmarkRepository.findByUserIdAndJpaQuestionIsNotNull(user.getId(), pageable);

        // Assert
        assertThat(bookmarksPage.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Find paginated bookmarks by user ID and non-null answer")
    void testFindByUserIdAndJpaAnswerIsNotNull() {
        // Arrange
        JpaBookmark bookmark = JpaBookmark.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .createdAt(LocalDateTime.now())
                .build();
        bookmarkRepository.save(bookmark);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<JpaBookmark> bookmarksPage = bookmarkRepository.findByUserIdAndJpaAnswerIsNotNull(user.getId(), pageable);

        // Assert
        assertThat(bookmarksPage.getTotalElements()).isEqualTo(1);
    }
}
