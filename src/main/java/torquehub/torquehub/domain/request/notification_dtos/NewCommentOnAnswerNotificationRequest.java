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
public class NewCommentOnAnswerNotificationRequest {

    @NotNull
    @Min(0)
    private Long questionId;

    @NotNull
    @Min(0)
    private Long answerId;

    @NotNull
    @Min(0)
    private Long userId;

    @NotBlank
    private String message;
}
