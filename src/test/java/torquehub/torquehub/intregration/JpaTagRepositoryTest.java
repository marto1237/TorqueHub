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
import torquehub.torquehub.persistence.jpa.impl.JpaTagRepository;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaTagRepository;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaTagRepository.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaTagRepositoryTest {

    // Start MySQL container for integration tests
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
    private SpringDataJpaTagRepository springDataJpaTagRepository;

    @Autowired
    private JpaTagRepository tagRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {

        springDataJpaTagRepository.deleteAll();
    }

    @Test
    @DisplayName("Save a new tag successfully")
    void testSaveNewTag() {
        JpaTag tag = JpaTag.builder()
                .name("TestTag")
                .usageCount(5)
                .build();

        JpaTag savedTag = tagRepository.save(tag);

        assertThat(savedTag.getId()).isNotNull();
        assertThat(savedTag.getName()).isEqualTo("TestTag");
        assertThat(savedTag.getUsageCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("Find tag by existing ID")
    void testFindByExistingId() {
        JpaTag tag = JpaTag.builder()
                .name("FindByIdTag")
                .usageCount(3)
                .build();
        JpaTag savedTag = tagRepository.save(tag);

        Optional<JpaTag> foundTag = tagRepository.findById(savedTag.getId());

        assertThat(foundTag).isPresent();
        assertThat(foundTag.get().getName()).isEqualTo("FindByIdTag");
    }

    @Test
    @DisplayName("Delete existing tag")
    void testDeleteExistingTag() {
        JpaTag tag = JpaTag.builder()
                .name("DeleteTag")
                .usageCount(2)
                .build();
        JpaTag savedTag = tagRepository.save(tag);

        boolean deleted = tagRepository.delete(savedTag);

        assertThat(deleted).isTrue();
        assertThat(tagRepository.findById(savedTag.getId())).isEmpty();
    }
}
