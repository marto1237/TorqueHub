package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.mapper.NotificationMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaNotification;
import torquehub.torquehub.domain.response.notification_dtos.NotificationResponse;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaNotificationRepository;
import torquehub.torquehub.persistence.repository.NotificationRepository;

import java.util.List;

@Repository
public class JpaNotificationRepository implements NotificationRepository {

    private final SpringDataJpaNotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public JpaNotificationRepository(SpringDataJpaNotificationRepository notificationRepository,
                                     NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public JpaNotification save(JpaNotification jpaNotification) {
        return notificationRepository.save(jpaNotification);
    }

    @Override
    public JpaNotification findById(Long notificationId) {
        return notificationRepository.findJpaNotificationById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
    }

    @Override
    public List<JpaNotification> findByJpaUserIdAndIsReadFalse(Long userId) {
        return notificationRepository.findByJpaUserIdAndIsReadFalse(userId);
    }

    @Override
    public Page<JpaNotification> findByJpaUserIdAndIsReadFalse(Long userId, Pageable pageable) {
        return notificationRepository.findByJpaUserIdAndIsReadFalse(userId, pageable);
    }


    @Override
    public Page<JpaNotification> findByJpaUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable) {
        return notificationRepository.findByJpaUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public Page<JpaNotification> findByJpaUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable) {
        return notificationRepository.findByJpaUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Cacheable(value = "topUnreadNotifications", key = "#userId")
    public List<NotificationResponse> findTop5ByUserIdUnread(Long userId) {
        return notificationRepository.findByJpaUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, Pageable.ofSize(5))
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        return notificationRepository.countByJpaUserIdAndIsReadFalse(userId);
    }

    @Override
    public void saveAll(List<JpaNotification> notifications) {
        notificationRepository.saveAll(notifications);
    }

}
