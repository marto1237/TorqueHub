package torquehub.torquehub.domain.response.reputation_dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReputationResponse {

    private Long userId;
    private int updatedReputationPoints;
    private String message;
}
