package torquehub.torquehub.domain.request.follow_dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowedQuestionRequest {
        @NotNull(message = "User id is required")
        private Long userId;

        private Pageable pageable;
}
