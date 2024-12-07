package torquehub.torquehub.domain.response.profile_dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;
import torquehub.torquehub.domain.response.user_dtos.UserResponse;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    private UserResponse user;
    private List<QuestionSummaryResponse> questions;
    private List<AnswerResponse> answers;
    private Long questionCount;
    private Long answerCount;
}
