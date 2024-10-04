package torquehub.torquehub.domain.request.QuestionDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionUpdateRequest {

    @NotBlank
    @Size(min = 3, max = 500,message = "Title must be between 3 and 500 characters")
    private String title;

    @NotBlank(message = "Description cannot be blank")
    @Size(min = 3, max = 100000, message = "Description must be at least 3 and 100000 characters")
    private String description;

    @NotNull(message = "At least one tag is required")
    private Set<String> tags;

    @NotNull(message = "User ID is required")
    private Long userId;
}
