package torquehub.torquehub.configuration.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Duration;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class VoteRateLimiterInterceptor implements HandlerInterceptor {

    private final Map<Long, Map<Long, LocalDateTime>> userVoteTimestamps = new ConcurrentHashMap<>();
    private static final Duration VOTE_COOLDOWN = Duration.ofSeconds(5); // set desired cooldown period

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long userId = (Long) request.getSession().getAttribute("userId");
        if (userId == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        Long targetId = getTargetIdFromRequest(request);
        if (targetId == null) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write("Invalid target ID in the request.");
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        // Initialize the map for the user if not present
        userVoteTimestamps.putIfAbsent(userId, new ConcurrentHashMap<>());

        // Check the last vote timestamp for this specific target
        LocalDateTime lastVoteTime = userVoteTimestamps.get(userId).get(targetId);
        if (lastVoteTime != null && Duration.between(lastVoteTime, now).compareTo(VOTE_COOLDOWN) < 0) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Vote request limit reached. Please try again later.");
            return false;
        }

        // Update the last vote time for this specific target
        userVoteTimestamps.get(userId).put(targetId, now);
        return true;
    }

    private Long getTargetIdFromRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String[] pathSegments = uri.split("/");

        // Assuming a URI pattern like /questions/{questionId}/upvote or /answers/{answerId}/downvote
        // Identify the segment with the target ID based on expected URL structure
        try {
            for (int i = 0; i < pathSegments.length; i++) {
                if ((pathSegments[i].equals("questions") || pathSegments[i].equals("answers") || pathSegments[i].equals("comments"))
                        && i + 1 < pathSegments.length) {
                    return Long.parseLong(pathSegments[i + 1]); // Parse the ID following the target type
                }
            }
        } catch (NumberFormatException e) {
            // Log warning about malformed URI or unexpected format
        }
        return null; // Return null if parsing fails or pattern doesn’t match
    }

    // Optional: Cleanup old entries every hour to free memory (use @Scheduled in Spring for this)
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanUpOldEntries() {
        LocalDateTime now = LocalDateTime.now();
        userVoteTimestamps.forEach((userId, targetMap) ->
                targetMap.entrySet().removeIf(entry -> Duration.between(entry.getValue(), now).toHours() > 24));
    }
}
