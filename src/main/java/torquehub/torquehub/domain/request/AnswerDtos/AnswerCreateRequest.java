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
public class AddAnswerRequest {

    @NotBlank
    @Size(min = 3, message = "Text must be atleast 3 characters")
    private String text;

    @NotBlank(message = "Question ID is required")
    private Long questionId;

    @NotNull(message = "User ID is required")
    private Long userId;
}
