package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import torquehub.torquehub.domain.model.jpa_models.JpaUserPromotionLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataJpaUserPromotionLogRepository extends JpaRepository<JpaUserPromotionLog, Long> {

    List<JpaUserPromotionLog> findByPromotedUserId(Long promotedUserId);

    List<JpaUserPromotionLog> findByPromoterUserId(Long promoterUserId);

    @Query("SELECT log FROM JpaUserPromotionLog log WHERE log.timestamp BETWEEN :startDate AND :endDate")
    List<JpaUserPromotionLog> findLogsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT log FROM JpaUserPromotionLog log WHERE log.promotedUserId = :userId ORDER BY log.timestamp DESC")
    Optional<JpaUserPromotionLog> findLatestPromotionLogByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(log) FROM JpaUserPromotionLog log WHERE log.promoterUserId = :promoterId")
    long countByPromoterUserId(@Param("promoterId") Long promoterId);

    List<JpaUserPromotionLog> findByNewRole(String newRole);
}
