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
import torquehub.torquehub.domain.model.jpa_models.JpaFollow;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.persistence.jpa.impl.JpaFollowRepository;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaFollowRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaFollowRepository.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaFollowRepositoryTest {

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
    private SpringDataJpaFollowRepository springDataJpaFollowRepository;

    @Autowired
    private JpaFollowRepository followRepository;

    @Autowired
    private EntityManager entityManager;

    private JpaUser user;
    private JpaUser followedUser;
    private JpaQuestion question;
    private JpaAnswer answer;

    @BeforeEach
    void setUp() {
        springDataJpaFollowRepository.deleteAll();

        // Create and persist role
        JpaRole role = JpaRole.builder()
                .name("USER_ROLE")
                .build();
        entityManager.persist(role);

        // Create and persist users
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

        followedUser = JpaUser.builder()
                .username("followeduser")
                .email("followeduser@example.com")
                .jpaRole(role)
                .password("password")
                .salt("randomSalt")
                .points(15)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(followedUser);

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
    @DisplayName("Save a new follow successfully")
    void testSaveNewFollow() {
        // Arrange
        JpaFollow follow = JpaFollow.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .followedAt(LocalDateTime.now())
                .isMuted(false)
                .build();

        // Act
        JpaFollow savedFollow = followRepository.save(follow);

        // Assert
        assertThat(savedFollow.getId()).isNotNull();
        assertThat(savedFollow.getJpaUser().getId()).isEqualTo(user.getId());
        assertThat(savedFollow.getJpaQuestion().getId()).isEqualTo(question.getId());
    }

    @Test
    @DisplayName("Find follow by ID")
    void testFindById() {
        // Arrange
        JpaFollow follow = JpaFollow.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .followedAt(LocalDateTime.now())
                .isMuted(false)
                .build();
        JpaFollow savedFollow = followRepository.save(follow);

        // Act
        Optional<JpaFollow> foundFollow = followRepository.findById(savedFollow.getId());

        // Assert
        assertThat(foundFollow).isPresent();
        assertThat(foundFollow.get().getJpaQuestion().getId()).isEqualTo(question.getId());
    }

    @Test
    @DisplayName("Find follows by user ID")
    void testFindByUserId() {
        // Arrange
        JpaFollow follow = JpaFollow.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .followedAt(LocalDateTime.now())
                .isMuted(false)
                .build();
        followRepository.save(follow);

        // Act
        List<JpaFollow> userFollows = followRepository.findByUserId(user.getId());

        // Assert
        assertThat(userFollows).hasSize(1);
    }

    @Test
    @DisplayName("Find follow by user ID and question ID")
    void testFindByUserIdAndQuestionId() {
        // Arrange
        JpaFollow follow = JpaFollow.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .followedAt(LocalDateTime.now())
                .isMuted(false)
                .build();
        followRepository.save(follow);

        // Act
        Optional<JpaFollow> foundFollow = followRepository.findByUserIdAndQuestionId(user.getId(), question.getId());

        // Assert
        assertThat(foundFollow).isPresent();
        assertThat(foundFollow.get().getJpaQuestion().getId()).isEqualTo(question.getId());
    }

    @Test
    @DisplayName("Delete follow by ID")
    void testDeleteById() {
        // Arrange
        JpaFollow follow = JpaFollow.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .followedAt(LocalDateTime.now())
                .isMuted(false)
                .build();
        JpaFollow savedFollow = followRepository.save(follow);

        // Act
        boolean deleted = followRepository.deleteById(savedFollow.getId());

        // Assert
        assertThat(deleted).isTrue();
        assertThat(followRepository.findById(savedFollow.getId())).isEmpty();
    }

    @Test
    @DisplayName("Find follows by question ID and not muted")
    void testFindByQuestionIdAndMutedFalse() {
        // Arrange
        JpaFollow follow = JpaFollow.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .followedAt(LocalDateTime.now())
                .isMuted(false)
                .build();
        followRepository.save(follow);

        // Act
        List<JpaFollow> follows = followRepository.findByQuestionIdAndMutedFalse(question.getId());

        // Assert
        assertThat(follows).hasSize(1);
    }

    @Test
    @DisplayName("Find paginated follows by user ID and question is not null")
    void testFindByUserIdAndQuestionIsNotNull() {
        // Arrange
        JpaFollow follow = JpaFollow.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .followedAt(LocalDateTime.now())
                .isMuted(false)
                .build();
        followRepository.save(follow);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<JpaFollow> followsPage = followRepository.findByUserIdAndJpaQuestionIsNotNull(user.getId(), pageable);

        // Assert
        assertThat(followsPage.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Save and retrieve all follows")
    void testSaveAllAndFindAll() {
        JpaFollow follow1 = JpaFollow.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .followedAt(LocalDateTime.now())
                .isMuted(false)
                .build();

        JpaFollow follow2 = JpaFollow.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .followedAt(LocalDateTime.now())
                .isMuted(true)
                .build();

        followRepository.saveAll(List.of(follow1, follow2));

        List<JpaFollow> allFollows = followRepository.findByUserId(user.getId());

        assertThat(allFollows).hasSize(2);
    }

    @Test
    @DisplayName("Delete all follows and validate deletion")
    void testDeleteAll() {
        JpaFollow follow1 = JpaFollow.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .followedAt(LocalDateTime.now())
                .isMuted(false)
                .build();

        JpaFollow follow2 = JpaFollow.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .followedAt(LocalDateTime.now())
                .isMuted(true)
                .build();

        followRepository.saveAll(List.of(follow1, follow2));

        boolean deleted = followRepository.deleteAll(List.of(follow1, follow2));

        assertThat(deleted).isTrue();
        assertThat(followRepository.findByUserId(user.getId())).isEmpty();
    }

    @Test
    @DisplayName("Find paginated follows by user ID and answer is not null")
    void testFindByUserIdAndAnswerIsNotNull() {
        JpaFollow follow = JpaFollow.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .followedAt(LocalDateTime.now())
                .isMuted(false)
                .build();
        followRepository.save(follow);

        Pageable pageable = PageRequest.of(0, 10);

        Page<JpaFollow> followsPage = followRepository.findByUserIdAndJpaAnswerIsNotNull(user.getId(), pageable);

        assertThat(followsPage.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Find follows by answer ID and muted false")
    void testFindByAnswerIdAndMutedFalse() {
        JpaFollow follow = JpaFollow.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .followedAt(LocalDateTime.now())
                .isMuted(false)
                .build();
        followRepository.save(follow);

        List<JpaFollow> follows = followRepository.findByAnswerIdAndMutedFalse(answer.getId());

        assertThat(follows).hasSize(1);
        assertThat(follows.get(0).isMuted()).isFalse();
    }
}