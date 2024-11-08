package torquehub.torquehub.domain.request.bookmark_dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookmarkAnswerRequest {
    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Answer id is required")
    private Long answerId;
}
