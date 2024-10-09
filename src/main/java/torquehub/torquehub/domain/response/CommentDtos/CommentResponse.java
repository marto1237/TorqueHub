package torquehub.torquehub.domain.response.CommentDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private String text;
    private String username;
    private int userPoints;
    private int votes;
    private boolean isEdited;
    private ReputationResponse reputationResponse;
    private Date postedTime;
}
