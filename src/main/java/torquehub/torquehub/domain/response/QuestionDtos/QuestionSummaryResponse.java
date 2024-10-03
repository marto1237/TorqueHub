package torquehub.torquehub.domain.response.QuestionDtos;

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
public class QuestionSummaryResponse {

    private Long id;
    private String title;
    private Set<String> tags;
    private String userName;
    private int userPoints;
    private int views;
    private int votes;
    private int totalAnswers;
    private LocalDateTime askedTime;
}
