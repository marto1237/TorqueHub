package torquehub.torquehub.domain.model.plain_models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseNotification {
    private Long id;
    private JpaUser jpaUser;
    private JpaUser voter;
    private Integer points;
    private String message;
    private LocalDateTime createdAt;
    private boolean isRead = false;
}
