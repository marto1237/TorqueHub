package torquehub.torquehub.domain.request.vote_dtos;

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
public class VoteCommentNotificationRequest {

    @NotNull
    @Min(0)
    private Long commentOwnerId;

    @NotBlank
    private String message;

    @NotNull
    private Long voterId;

    @NotNull
    private Long commentId;
}
