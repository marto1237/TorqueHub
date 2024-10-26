package torquehub.torquehub.domain.request.follow_dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowQuestionRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Question ID is required")
    private Long questionId;
}
