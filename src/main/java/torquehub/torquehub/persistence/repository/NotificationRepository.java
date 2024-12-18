package torquehub.torquehub.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import torquehub.torquehub.domain.model.jpa_models.JpaNotification;
import torquehub.torquehub.domain.response.notification_dtos.NotificationResponse;

import java.util.List;


public interface NotificationRepository {
     JpaNotification save(JpaNotification jpaNotification);
     JpaNotification findById(Long id);
     List<JpaNotification> findByJpaUserIdAndIsReadFalse(Long userId);
     Page<JpaNotification> findByJpaUserIdAndIsReadFalse(Long userId, Pageable pageable);
     Page<JpaNotification> findByJpaUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
     Page<JpaNotification> findByJpaUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);
     List<NotificationResponse> findTop5ByUserIdUnread(Long userId);
     long countUnreadByUserId(Long userId);

     void saveAll(List<JpaNotification> notifications);
}
