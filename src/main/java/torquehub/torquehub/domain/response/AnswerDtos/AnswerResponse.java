package torquehub.torquehub.domain.response.AnswerDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import torquehub.torquehub.domain.response.CommentDtos.CommentResponse;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnswerResponse {
    private Long id;
    private String text;
    private String username;
    private int userPoints;
    private int votes;
    private boolean isEdited;
    private List<CommentResponse> comments;
    private ReputationResponse reputationUpdate;
    private Date postedTime;
}
