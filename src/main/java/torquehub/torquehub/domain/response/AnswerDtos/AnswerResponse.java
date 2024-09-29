package torquehub.torquehub.domain.response.AnswerDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnswerResponse {
    private Long id;
    private String text;
    private String username;
    private int votes;
    private boolean isEdited;
    private Date postedTime;
}
