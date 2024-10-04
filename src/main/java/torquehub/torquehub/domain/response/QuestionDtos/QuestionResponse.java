package torquehub.torquehub.domain.response.QuestionDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import torquehub.torquehub.domain.model.Tag;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponse {
    private Long id;
    private String title;
    private String description;
    private Set<String> tags;
    private int views;
    private int votes;
    private int totalAnswers;
    private String username;
    private ReputationResponse reputationUpdate;
    private LocalDateTime askedTime;
}
