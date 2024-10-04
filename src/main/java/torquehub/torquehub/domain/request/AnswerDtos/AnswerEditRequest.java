package torquehub.torquehub.domain.request.AnswerDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditAnswerRequest {

    @NotBlank
    @Size(min = 3, message = "Text must be at least 3 characters")
    private String text;


}
