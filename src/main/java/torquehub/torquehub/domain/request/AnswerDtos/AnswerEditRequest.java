package torquehub.torquehub.domain.request.AnswerDtos;

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
public class AnswerEditRequest {

    @NotBlank
    @Size(min = 3, max = 100000, message = "Text must be between 3 and 100000 characters")
    private String text;


}
