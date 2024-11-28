package torquehub.torquehub.business.interfaces;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import torquehub.torquehub.domain.request.event_dtos.EventCreateRequest;
import torquehub.torquehub.domain.response.event_dtos.EventResponse;

@Service
public class TicketApiService {

    private final RestTemplate restTemplate;

    // Base URL of the Ticket API
    @Value("${ticket.api.base-url}") // Inject the value from application.properties
    private String ticketApiBaseUrl;

    public TicketApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public EventResponse createEvent(EventCreateRequest request) {
        String url = ticketApiBaseUrl + "/events";
        ResponseEntity<EventResponse> response = restTemplate.postForEntity(url, request, EventResponse.class);
        return response.getBody();
    }

    public boolean purchaseTicket(Long userId, Long eventId) {
        String url = ticketApiBaseUrl + "/tickets";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("userId", userId)
                .queryParam("eventId", eventId);

        ResponseEntity<Void> response = restTemplate.postForEntity(builder.toUriString(), null, Void.class);
        return response.getStatusCode().is2xxSuccessful();
    }
}
