package torquehub.torquehub.domain.request.FollowDtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowAnswerRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Answer ID is required")
    private Long answerId;
}
