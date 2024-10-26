package torquehub.torquehub.domain.response.bookmark_dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookmarkResponse {
    private Long id;
    private Long userId;
    private Long questionId;
    private Long answerId;
    private LocalDateTime createdAt;
}
