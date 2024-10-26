package torquehub.torquehub.domain.request.user_promotion_dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPromotionRequest {

    @NotNull()
    private Long promotedUserId;

    @NotNull
    private Long promoterUserId;

    @NotBlank()
    private String newRole;

    private LocalDateTime timestamp;

}
