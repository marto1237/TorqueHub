package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.jpa_models.JpaNotification;

import java.util.List;
import java.util.Optional;

public interface SpringDataJpaNotificationRepository extends JpaRepository<JpaNotification, Long> {

    List<JpaNotification> findByJpaUserIdAndIsReadFalse(Long userId);
    Page<JpaNotification> findByJpaUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<JpaNotification> findByJpaUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<JpaNotification> findByJpaUserIdAndIsReadFalse(Long userId, Pageable pageable);
    Optional<JpaNotification> findJpaNotificationById (Long id);
    long countByJpaUserIdAndIsReadFalse(Long userId);


}
