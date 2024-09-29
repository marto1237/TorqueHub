package torquehub.torquehub.domain.response.QuestionDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import torquehub.torquehub.domain.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponse {
    private Long id;
    private String title;
    private String description;
    private List<String> tags;
    private int views;
    private int votes;
    private int totalAnswers;
    private User user;
    private LocalDateTime askedTime;
}