package torquehub.torquehub.domain.request.ReputationDtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReputationDataUpdateRequest {

    @NotNull(message = "User id is required")
    private Long userId;
}
