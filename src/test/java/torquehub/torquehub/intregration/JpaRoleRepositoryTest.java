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
import torquehub.torquehub.persistence.jpa.impl.JpaRoleRepository;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaRoleRepository;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaRoleRepository.class) // Import the custom implementation of RoleRepository
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaRoleRepositoryTest {

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
    private SpringDataJpaRoleRepository springDataJpaRoleRepository;

    @Autowired
    private JpaRoleRepository roleRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        springDataJpaRoleRepository.deleteAll(); // Clean the database before each test
    }

    @Test
    @DisplayName("Save a new role successfully")
    void testSaveNewRole() {
        // Arrange
        JpaRole role = JpaRole.builder()
                .name("ADMIN")
                .build();

        // Act
        JpaRole savedRole = roleRepository.save(role);

        // Assert
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getName()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Find role by existing ID")
    void testFindByExistingId() {
        // Arrange
        JpaRole role = JpaRole.builder()
                .name("USER")
                .build();
        JpaRole savedRole = roleRepository.save(role);

        // Act
        Optional<JpaRole> foundRole = roleRepository.findById(savedRole.getId());

        // Assert
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo("USER");
    }

    @Test
    @DisplayName("Find role by name")
    void testFindByName() {
        // Arrange
        JpaRole role = JpaRole.builder()
                .name("MODERATOR")
                .build();
        roleRepository.save(role);

        // Act
        Optional<JpaRole> foundRole = roleRepository.findByName("MODERATOR");

        // Assert
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo("MODERATOR");
    }

    @Test
    @DisplayName("Delete existing role")
    void testDeleteExistingRole() {
        // Arrange
        JpaRole role = JpaRole.builder()
                .name("ORGANIZER")
                .build();
        JpaRole savedRole = roleRepository.save(role);

        // Act
        boolean deleted = roleRepository.delete(savedRole);

        // Assert
        assertThat(deleted).isTrue();
        assertThat(roleRepository.findById(savedRole.getId())).isEmpty();
    }

    @Test
    @DisplayName("Find all roles")
    void testFindAllRoles() {
        // Arrange
        JpaRole role1 = JpaRole.builder().name("ROLE1").build();
        JpaRole role2 = JpaRole.builder().name("ROLE2").build();
        roleRepository.save(role1);
        roleRepository.save(role2);

        // Act
        var allRoles = roleRepository.findAll();

        // Assert
        assertThat(allRoles).hasSize(2);
        assertThat(allRoles).extracting(JpaRole::getName)
                .containsExactlyInAnyOrder("ROLE1", "ROLE2");
    }
}
