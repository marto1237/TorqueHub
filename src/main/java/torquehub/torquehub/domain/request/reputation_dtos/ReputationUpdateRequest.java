package torquehub.torquehub.domain.request.reputation_dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReputationUpdateRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Updated reputation points is required")
    private int points;
}
