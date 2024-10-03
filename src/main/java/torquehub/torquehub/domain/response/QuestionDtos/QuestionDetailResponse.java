package torquehub.torquehub.domain.response.QuestionDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import torquehub.torquehub.domain.response.AnswerDtos.AnswerResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDetailResponse {

    private Long id;
    private String title;
    private String description;
    private Set<String> tags;
    private String userName;
    private int userPoints;
    private int views;
    private int votes;
    private List<AnswerResponse> answers;
    private LocalDateTime askedTime;
}
