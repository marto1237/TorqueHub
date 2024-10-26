package torquehub.torquehub.persistence.repository;

import torquehub.torquehub.domain.model.jpa_models.JpaUserPromotionLog;

import java.util.List;
import java.util.Optional;

public interface UserPromotionLogRepository {
    List<JpaUserPromotionLog> findAll();
    JpaUserPromotionLog save(JpaUserPromotionLog userPromotionLog);
    Optional<JpaUserPromotionLog> findById(Long id);
    boolean delete(JpaUserPromotionLog userPromotionLog);
    boolean existsById(Long id);
}
