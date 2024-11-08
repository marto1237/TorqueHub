package torquehub.torquehub.business.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.utils.VoteRateLimiterInterceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimiterTest {
    @Mock
    private AccessTokenDecoder accessTokenDecoder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AccessToken mockToken;

    @InjectMocks
    private VoteRateLimiterInterceptor rateLimiterInterceptor;


    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        rateLimiterInterceptor = new VoteRateLimiterInterceptor(accessTokenDecoder);

        // Set up mock AccessToken with a user ID
        when(mockToken.getUserID()).thenReturn(1L);

        // Set up the response writer to capture output
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }


    @Test
    void testAllowVote_Success() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(accessTokenDecoder.decode("valid-token")).thenReturn(mockToken);
        when(request.getRequestURI()).thenReturn("/questions/123/upvote");

        // Act
        boolean result = rateLimiterInterceptor.preHandle(request, response, new Object());

        // Assert
        assertEquals(true, result);
    }

    @Test
    void testRateLimitWithinCooldown() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(accessTokenDecoder.decode("valid-token")).thenReturn(mockToken);
        when(request.getRequestURI()).thenReturn("/questions/123/upvote");

        // First vote - allow
        rateLimiterInterceptor.preHandle(request, response, new Object());

        // Act - Second vote request within cooldown
        boolean result = rateLimiterInterceptor.preHandle(request, response, new Object());

        // Assert - Should be rate-limited
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        assertEquals(false, result);
    }


    @Test
    void testUnauthorized_NoAuthorizationHeader() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        boolean result = rateLimiterInterceptor.preHandle(request, response, new Object());

        // Assert - Should be unauthorized
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        assertEquals(false, result);
    }

    @Test
    void testUnauthorized_InvalidToken() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(accessTokenDecoder.decode("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

        // Act
        boolean result = rateLimiterInterceptor.preHandle(request, response, new Object());

        // Assert - Should be unauthorized
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        assertEquals(false, result);
    }

    @Test
    void testInvalidActionInRequest() throws Exception {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(accessTokenDecoder.decode("valid-token")).thenReturn(mockToken);
        when(request.getRequestURI()).thenReturn("/invalid/endpoint");

        // Act
        boolean result = rateLimiterInterceptor.preHandle(request, response, new Object());

        // Assert - Should be bad request
        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        assertEquals(false, result);
    }


}