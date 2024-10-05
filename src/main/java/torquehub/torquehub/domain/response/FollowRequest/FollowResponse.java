package torquehub.torquehub.domain.response.FollowRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowResponse {

    private Long id;
    private Long userId;
    private Long questionId;
    private boolean isMuted;
    private LocalDateTime followedAt;
}
