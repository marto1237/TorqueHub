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
import torquehub.torquehub.domain.mapper.NotificationMapperImpl;
import torquehub.torquehub.domain.model.jpa_models.JpaNotification;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.persistence.jpa.impl.JpaNotificationRepository;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaNotificationRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import({JpaNotificationRepository.class, NotificationMapperImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaNotificationRepositoryTest {

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
    private SpringDataJpaNotificationRepository springDataJpaNotificationRepository;

    @Autowired
    private JpaNotificationRepository notificationRepository;

    @Autowired
    private EntityManager entityManager;

    private JpaUser user;

    @BeforeEach
    void setUp() {
        springDataJpaNotificationRepository.deleteAll();

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
                .build();
        entityManager.persist(user);
    }

    @Test
    @DisplayName("Save a new notification successfully")
    void testSaveNewNotification() {
        // Arrange
        JpaNotification notification = JpaNotification.builder()
                .jpaUser(user)
                .message("New Notification")
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();

        // Act
        JpaNotification savedNotification = notificationRepository.save(notification);

        // Assert
        assertThat(savedNotification.getId()).isNotNull();
        assertThat(savedNotification.getJpaUser().getId()).isEqualTo(user.getId());
        assertThat(savedNotification.getMessage()).isEqualTo("New Notification");
    }

    @Test
    @DisplayName("Find notification by ID")
    void testFindById() {
        // Arrange
        JpaNotification notification = JpaNotification.builder()
                .jpaUser(user)
                .message("Find by ID Notification")
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
        JpaNotification savedNotification = notificationRepository.save(notification);

        // Act
        JpaNotification foundNotification = notificationRepository.findById(savedNotification.getId());

        // Assert
        assertThat(foundNotification).isNotNull();
        assertThat(foundNotification.getMessage()).isEqualTo("Find by ID Notification");
    }

    @Test
    @DisplayName("Find unread notifications by user ID")
    void testFindByJpaUserIdAndIsReadFalse() {
        // Arrange
        JpaNotification notification = JpaNotification.builder()
                .jpaUser(user)
                .message("Unread Notification")
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
        notificationRepository.save(notification);

        // Act
        List<JpaNotification> unreadNotifications = notificationRepository.findByJpaUserIdAndIsReadFalse(user.getId());

        // Assert
        assertThat(unreadNotifications).hasSize(1);
        assertThat(unreadNotifications.get(0).getMessage()).isEqualTo("Unread Notification");
    }

    @Test
    @DisplayName("Find notifications by user ID ordered by created date")
    void testFindByJpaUserIdOrderByCreatedAtDesc() {
        // Arrange
        JpaNotification notification1 = JpaNotification.builder()
                .jpaUser(user)
                .message("First Notification")
                .createdAt(LocalDateTime.now().minusDays(1))
                .isRead(false)
                .build();
        JpaNotification notification2 = JpaNotification.builder()
                .jpaUser(user)
                .message("Second Notification")
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
        notificationRepository.save(notification1);
        notificationRepository.save(notification2);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<JpaNotification> notificationsPage = notificationRepository.findByJpaUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        // Assert
        assertThat(notificationsPage.getTotalElements()).isEqualTo(2);
        assertThat(notificationsPage.getContent().get(0).getMessage()).isEqualTo("Second Notification");
    }

    @Test
    @DisplayName("Count unread notifications by user ID")
    void testCountUnreadByUserId() {
        // Arrange
        JpaNotification notification1 = JpaNotification.builder()
                .jpaUser(user)
                .message("Unread Notification 1")
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
        JpaNotification notification2 = JpaNotification.builder()
                .jpaUser(user)
                .message("Unread Notification 2")
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
        notificationRepository.save(notification1);
        notificationRepository.save(notification2);

        // Act
        long unreadCount = notificationRepository.countUnreadByUserId(user.getId());

        // Assert
        assertThat(unreadCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Find top 5 unread notifications for user")
    void testFindTop5ByUserIdUnread() {
        // Arrange
        for (int i = 1; i <= 6; i++) {
            JpaNotification notification = JpaNotification.builder()
                    .jpaUser(user)
                    .message("Unread Notification " + i)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        }

        // Act
        List<JpaNotification> topNotifications = notificationRepository.findByJpaUserIdAndIsReadFalse(user.getId(), PageRequest.of(0, 5)).getContent();

        // Assert
        assertThat(topNotifications).hasSize(5);
        assertThat(topNotifications.get(0).getMessage()).isEqualTo("Unread Notification 1");
    }
}
