package torquehub.torquehub.domain.model.plain_models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BasedUsePromotionLog {
    private Long id;
    private Long promotedUserId;
    private Long promoterUserId;
    private String newRole;
    private LocalDateTime timestamp;
}
