package torquehub.torquehub.domain.request.tag_dtos;

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
public class TagUpdateRequest {

        @NotNull
        private Long id;

        @NotBlank
        @Size(min = 3, max = 50,message = "Tag name must be between 3 and 50 characters")
        private String name;
}
