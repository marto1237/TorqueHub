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
import torquehub.torquehub.domain.model.jpa_models.JpaRole;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.persistence.jpa.impl.JpaUserRepository;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaUserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaUserRepository.class) // Import the custom implementation of UserRepository
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaUserRepositoryTest {

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
    private SpringDataJpaUserRepository springDataJpaUserRepository;

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private JpaRole defaultRole;

    @BeforeEach
    void setUp() {
        springDataJpaUserRepository.deleteAll();

        // Setup a default role for users
        defaultRole = JpaRole.builder()
                .name("USER")
                .build();
        entityManager.persist(defaultRole);
    }

    @Test
    @DisplayName("Save a new user successfully")
    void testSaveNewUser() {
        // Arrange
        JpaUser user = JpaUser.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .salt("randomSalt")
                .jpaRole(defaultRole)
                .points(10)
                .build();

        // Act
        JpaUser savedUser = userRepository.save(user);

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("testuser@example.com");
        assertThat(savedUser.getJpaRole().getName()).isEqualTo("USER");
        assertThat(savedUser.getPoints()).isEqualTo(10);
    }

    @Test
    @DisplayName("Find user by existing ID")
    void testFindByExistingId() {
        // Arrange
        JpaUser user = JpaUser.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .salt("randomSalt")
                .jpaRole(defaultRole)
                .build();
        JpaUser savedUser = userRepository.save(user);

        // Act
        Optional<JpaUser> foundUser = userRepository.findById(savedUser.getId());

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Find user by username")
    void testFindByUsername() {
        // Arrange
        JpaUser user = JpaUser.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .salt("randomSalt")
                .jpaRole(defaultRole)
                .build();
        userRepository.save(user);

        // Act
        Optional<JpaUser> foundUser = userRepository.findByUsername("testuser");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("testuser@example.com");
    }

    @Test
    @DisplayName("Check existence by username")
    void testExistsByUsername() {
        // Arrange
        JpaUser user = JpaUser.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .salt("randomSalt")
                .jpaRole(defaultRole)
                .build();
        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByUsername("testuser");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Delete an existing user")
    void testDeleteExistingUser() {
        // Arrange
        JpaUser user = JpaUser.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .salt("randomSalt")
                .jpaRole(defaultRole)
                .build();
        JpaUser savedUser = userRepository.save(user);

        // Act
        boolean deleted = userRepository.delete(savedUser);

        // Assert
        assertThat(deleted).isTrue();
        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("Find all users")
    void testFindAllUsers() {
        // Arrange
        JpaUser user1 = JpaUser.builder()
                .username("user1")
                .email("user1@example.com")
                .password("password1")
                .salt("salt1")
                .jpaRole(defaultRole)
                .build();

        JpaUser user2 = JpaUser.builder()
                .username("user2")
                .email("user2@example.com")
                .password("password2")
                .salt("salt2")
                .jpaRole(defaultRole)
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        // Act
        var allUsers = userRepository.findAll();

        // Assert
        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting(JpaUser::getUsername)
                .containsExactlyInAnyOrder("user1", "user2");
    }
}
