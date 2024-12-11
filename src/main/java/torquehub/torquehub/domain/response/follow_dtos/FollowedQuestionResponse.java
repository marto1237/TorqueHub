package torquehub.torquehub.domain.response.follow_dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowedQuestionResponse {
    private Long followId;
    private Long questionId;
    private String title;
    private String description;
    private Set<String> tags;
    private int views;
    private int votes;
    private int totalAnswers;
    private String username;
    private boolean isMuted;
    private LocalDateTime askedTime;
}
