package torquehub.torquehub.domain.request.answer_dtos;

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
public class AnswerEditRequest {

    @NotNull
    private Long userId;

    @NotBlank
    @Size(min = 3, max = 100000, message = "Text must be between 3 and 100000 characters")
    private String text;


}
