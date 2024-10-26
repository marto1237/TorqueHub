package torquehub.torquehub.domain.request.comment_dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentEditRequest {

    @NotBlank
    @Size(min = 3,max = 100000, message = "Comment text must be between 3 and 100000 characters")
    private String text;
}
