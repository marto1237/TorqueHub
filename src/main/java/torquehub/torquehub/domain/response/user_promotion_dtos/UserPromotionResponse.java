package torquehub.torquehub.domain.response.user_promotion_dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPromotionResponse {

    private Long promotedUserId;

    private Long promoterUserId;

    private String newRole;

    private LocalDateTime timestamp;

    private String message;
}
