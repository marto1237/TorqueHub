package torquehub.torquehub.domain.response.notification_dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {

    private Long id;
    private String message;
    private Long userId;
    private Long voterId;
    private Integer points;
    private LocalDateTime createdAt;
    private boolean isRead;
}
