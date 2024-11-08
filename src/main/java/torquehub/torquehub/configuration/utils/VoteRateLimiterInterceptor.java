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
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;

@Component
public class VoteRateLimiterInterceptor implements HandlerInterceptor {

    private final Map<Long, Map<String, LocalDateTime>> userActionTimestamps = new ConcurrentHashMap<>();
    private static final Duration VOTE_COOLDOWN = Duration.ofSeconds(5); // set desired cooldown period

    private final AccessTokenDecoder accessTokenDecoder;

    public VoteRateLimiterInterceptor(AccessTokenDecoder accessTokenDecoder) {
        this.accessTokenDecoder = accessTokenDecoder;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow preflight CORS requests (OPTIONS) to pass through
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Get the JWT token from the Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        String token = authHeader.substring(7);
        Long userId;
        try {
            // Decode the token to get the user ID
            AccessToken accessToken = accessTokenDecoder.decode(token);
            userId = accessToken.getUserID();
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        String actionKey = getActionKeyFromRequest(request);
        if (actionKey == null) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write("Invalid action in the request.");
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        // Initialize the map for the user if not present
        userActionTimestamps.putIfAbsent(userId, new ConcurrentHashMap<>());

        // Check the last vote timestamp for this specific target
        LocalDateTime lastVoteTime = userActionTimestamps.get(userId).get(actionKey);
        if (lastVoteTime != null && Duration.between(lastVoteTime, now).compareTo(VOTE_COOLDOWN) < 0) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Vote request limit reached. Please try again later.");
            return false;
        }

        // Update the last vote time for this specific target
        userActionTimestamps.get(userId).put(actionKey, now);
        return true;
    }

    private String getActionKeyFromRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // Define unique keys based on endpoints
        if (uri.matches(".*/questions/\\d+/upvote") || uri.matches(".*/questions/\\d+/downvote")) {
            return "vote-question";
        } else if (uri.matches(".*/answers/\\d+/upvote") || uri.matches(".*/answers/\\d+/downvote")) {
            return "vote-answer";
        } else if (uri.matches(".*/bookmarks/\\d+")) {
            return "bookmark-question";
        } else if (uri.matches(".*/bookmarks/answer")) {
            return "bookmark-answer";
        } else if (uri.matches(".*/follows/questions/\\d+")) {
            return "follow-question";
        } else if (uri.matches(".*/follows/answers/\\d+")) {
            return "follow-answer";
        }
        return null;
    }

    // Optional: Cleanup old entries every hour to free memory (use @Scheduled in Spring for this)
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanUpOldEntries() {
        LocalDateTime now = LocalDateTime.now();
        userActionTimestamps.forEach((userId, targetMap) ->
                targetMap.entrySet().removeIf(entry -> Duration.between(entry.getValue(), now).toHours() > 24));
    }
}
