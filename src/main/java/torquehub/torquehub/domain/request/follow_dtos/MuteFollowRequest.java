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
public class MuteFollowRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Follow id is required")
    private Long followId;

    @NotNull(message = "Mute status is required")
    private Boolean muteStatus;
}
