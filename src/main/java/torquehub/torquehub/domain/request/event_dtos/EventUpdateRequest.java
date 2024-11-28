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
public class EventUpdateRequest {

    @NotNull(message = "Event id cannot be null")
    private Long id;

    @NotBlank(message = "Event name cannot be blank")
    private String name;

    @NotBlank(message = "Event location cannot be blank")
    private String location;

    @NotNull(message = "Creator id cannot be null")
    private Long creatorUserId;

    @NotNull(message = "Event start time cannot be null")
    private LocalDateTime newTime;

    @NotBlank(message = "Ticket Types cannot be blank")
    private Set<String> ticketTypes = new HashSet<>();
}
