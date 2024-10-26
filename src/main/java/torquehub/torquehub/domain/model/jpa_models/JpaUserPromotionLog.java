package torquehub.torquehub.domain.model.jpa_models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import torquehub.torquehub.domain.model.plain_models.BasedUsePromotionLog;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_promotion_logs")
public class JpaUserPromotionLog extends BasedUsePromotionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "promoted_user_id", nullable = false)
    private Long promotedUserId;

    @Column(name = "promoter_user_id", nullable = false)
    private Long promoterUserId;

    @Column(name = "new_role", nullable = false)
    private String newRole;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

}
