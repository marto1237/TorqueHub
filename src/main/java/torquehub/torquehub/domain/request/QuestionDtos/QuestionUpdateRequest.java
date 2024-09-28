package torquehub.torquehub.domain.request.QuestionDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionUpdateRequest {

    @NotBlank
    @Size(min = 3, max = 250,message = "Title must be between 3 and 250 characters")
    private String title;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    private List<String> tags;

    @NotBlank(message = "User ID is required")
    private Long userId;
}
