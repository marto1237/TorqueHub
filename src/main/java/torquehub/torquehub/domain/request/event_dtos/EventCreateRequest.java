package torquehub.torquehub.domain.request.event_dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventCreateRequest {

    @NotBlank(message = "Event name cannot be blank")
    private String name;

    @NotBlank(message = "User ID is required")
    private String location;

    @NotNull(message = "Creator id cannot be null")
    private Long creatorUserId;

    @NotNull(message = "Event date and time are required")
    private LocalDateTime date;

    private Set<String> ticketTypes = new HashSet<>();
}
