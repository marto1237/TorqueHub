package torquehub.torquehub.domain.request.notification_dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PointsNotificationRequest {

    @NotNull(message = "Recipient is required")
    private JpaUser recipient;

    @NotNull
    @Min(0)
    private int points;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotNull(message = "Voter is required")
    private JpaUser voter;
}
