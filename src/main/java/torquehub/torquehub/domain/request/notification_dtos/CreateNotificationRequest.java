package torquehub.torquehub.domain.request.notification_dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateNotificationRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Voter id is required")
    private Long voterId;

    @NotNull(message = "Points is required")
    private int points;

    @NotBlank(message = "Message is required")
    private String message;
}
