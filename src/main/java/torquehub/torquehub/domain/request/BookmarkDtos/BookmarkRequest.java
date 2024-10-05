package torquehub.torquehub.domain.request.BookmarkDtos;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookmarkRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Question id is required")
    private Long questionId;

    @NotNull(message = "User id is required")
    private Long answerId;
}
