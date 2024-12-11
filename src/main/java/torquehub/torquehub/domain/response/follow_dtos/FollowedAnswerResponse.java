package torquehub.torquehub.domain.response.follow_dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowedAnswerResponse {
    private Long followId;
    private Long answerId;
    private String text;
    private String username;
    private int votes;
    private boolean isMuted;
    private boolean isEdited;
    private LocalDateTime postedTime;
}
