package torquehub.torquehub.domain.request.notification_dtos;

import jakarta.validation.constraints.Min;
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
public class CreateCommentAnswerRequest {

    @NotNull
    @Min(0)
    private Long userId;

    @NotNull
    @Min(0)
    private Long voterId;

    @NotBlank
    private String message;

    @Min(0)
    private int points;
}
