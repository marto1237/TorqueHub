package torquehub.torquehub.intregration;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import torquehub.torquehub.domain.model.jpa_models.*;
import torquehub.torquehub.persistence.jpa.impl.JpaVoteRepository;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaVoteRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaVoteRepository.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaVoteRepositoryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpassword");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private JpaVoteRepository voteRepository;

    @Autowired
    private SpringDataJpaVoteRepository springDataJpaVoteRepository;

    @Autowired
    private EntityManager entityManager;

    private JpaUser user;
    private JpaQuestion question;
    private JpaAnswer answer;
    private JpaComment comment;

    @BeforeEach
    void setUp() {
        springDataJpaVoteRepository.deleteAll();

        JpaRole role = JpaRole.builder()
                .name("USER")
                .build();
        entityManager.persist(role);

        // Initialize test data
        user = JpaUser.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password")
                .salt("salt")
                .points(10)
                .jpaRole(role)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(user);

        question = JpaQuestion.builder()
                .title("Test Question")
                .description("This is a test question.")
                .jpaUser(user)
                .askedTime(LocalDateTime.now())
                .lastActivityTime(LocalDateTime.now())
                .build();
        entityManager.persist(question);

        answer = JpaAnswer.builder()
                .text("Test Answer")
                .jpaQuestion(question)
                .jpaUser(user)
                .answeredTime(LocalDateTime.now())
                .build();
        entityManager.persist(answer);

        comment = JpaComment.builder()
                .text("Test Comment")
                .jpaAnswer(answer)
                .jpaUser(user)
                .commentedTime(LocalDateTime.now())
                .build();
        entityManager.persist(comment);
    }

    @Test
    @DisplayName("Save a new vote successfully")
    void testSaveNewVote() {
        JpaVote vote = JpaVote.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .upvote(true)
                .votedAt(LocalDateTime.now())
                .build();

        JpaVote savedVote = voteRepository.save(vote);

        assertThat(savedVote.getId()).isNotNull();
        assertThat(savedVote.isUpvote()).isTrue();
        assertThat(savedVote.getJpaUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("Find vote by user and answer")
    void testFindByUserAndAnswer() {
        JpaVote vote = JpaVote.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .upvote(true)
                .votedAt(LocalDateTime.now())
                .build();
        entityManager.persist(vote);

        Optional<JpaVote> foundVote = voteRepository.findByUserAndJpaAnswer(user, answer);

        assertThat(foundVote).isPresent();
        assertThat(foundVote.get().isUpvote()).isTrue();
    }

    @Test
    @DisplayName("Find vote by user ID and answer ID")
    void testFindByUserIdAndAnswerId() {
        JpaVote vote = JpaVote.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .upvote(true)
                .votedAt(LocalDateTime.now())
                .build();
        entityManager.persist(vote);

        Optional<JpaVote> foundVote = voteRepository.findByUserIdAndAnswerId(user.getId(), answer.getId());

        assertThat(foundVote).isPresent();
        assertThat(foundVote.get().getJpaAnswer()).isEqualTo(answer);
    }

    @Test
    @DisplayName("Delete an existing vote")
    void testDeleteVote() {
        JpaVote vote = JpaVote.builder()
                .jpaUser(user)
                .jpaAnswer(answer)
                .upvote(false)
                .votedAt(LocalDateTime.now())
                .build();
        entityManager.persist(vote);

        boolean deleted = voteRepository.delete(vote);

        assertThat(deleted).isTrue();
        assertThat(voteRepository.findByUserAndJpaAnswer(user, answer)).isEmpty();
    }

    @Test
    @DisplayName("Find top vote by user and question ordered by votedAt descending")
    void testFindTopByUserAndQuestion() {
        JpaVote vote1 = JpaVote.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .upvote(true)
                .votedAt(LocalDateTime.now().minusDays(1))
                .build();
        JpaVote vote2 = JpaVote.builder()
                .jpaUser(user)
                .jpaQuestion(question)
                .upvote(false)
                .votedAt(LocalDateTime.now())
                .build();
        entityManager.persist(vote1);
        entityManager.persist(vote2);

        Optional<JpaVote> topVote = voteRepository.findTopByJpaUserAndJpaQuestionOrderByVotedAtDesc(user, question);

        assertThat(topVote).isPresent();
        assertThat(topVote.get().isUpvote()).isFalse();
    }

    @Test
    @DisplayName("Find vote by user and comment")
    void testFindByUserAndComment() {
        JpaVote vote = JpaVote.builder()
                .jpaUser(user)
                .jpaComment(comment)
                .upvote(true)
                .votedAt(LocalDateTime.now())
                .build();
        entityManager.persist(vote);

        Optional<JpaVote> foundVote = voteRepository.findByUserAndJpaComment(user, comment);

        assertThat(foundVote).isPresent();
        assertThat(foundVote.get().getJpaComment()).isEqualTo(comment);
    }

    @Test
    @DisplayName("Find vote by user ID and comment ID")
    void testFindByUserIdAndCommentId() {
        JpaVote vote = JpaVote.builder()
                .jpaUser(user)
                .jpaComment(comment)
                .upvote(false)
                .votedAt(LocalDateTime.now())
                .build();
        entityManager.persist(vote);

        Optional<JpaVote> foundVote = voteRepository.findByUserIdAndCommentId(user.getId(), comment.getId());

        assertThat(foundVote).isPresent();
        assertThat(foundVote.get().isUpvote()).isFalse();
    }
}
