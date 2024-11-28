package torquehub.torquehub.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import torquehub.torquehub.configuration.jwt.auth.RequestAuthenticatedUserProvider;
import torquehub.torquehub.configuration.jwt.token.AccessToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestAuthenticatedUserProviderTest {

    private RequestAuthenticatedUserProvider provider;

    @BeforeEach
    void setUp() {
        provider = new RequestAuthenticatedUserProvider();
    }

    @Test
    void shouldReturnNullWhenSecurityContextIsNull() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(null);

            AccessToken accessToken = provider.getAuthenticatedUserInRequest();

            assertNull(accessToken, "Expected accessToken to be null when SecurityContext is null.");
        }
    }

    @Test
    void shouldReturnNullWhenAuthenticationIsNull() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            SecurityContext mockContext = mock(SecurityContext.class);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(mockContext);

            when(mockContext.getAuthentication()).thenReturn(null);

            AccessToken accessToken = provider.getAuthenticatedUserInRequest();

            assertNull(accessToken, "Expected accessToken to be null when Authentication is null.");
        }
    }

    @Test
    void shouldReturnNullWhenDetailsAreNotAccessToken() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            SecurityContext mockContext = mock(SecurityContext.class);
            Authentication mockAuthentication = mock(Authentication.class);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(mockContext);
            when(mockContext.getAuthentication()).thenReturn(mockAuthentication);
            when(mockAuthentication.getDetails()).thenReturn("Invalid Details Object");

            AccessToken accessToken = provider.getAuthenticatedUserInRequest();

            assertNull(accessToken, "Expected accessToken to be null when details are not an AccessToken.");
        }
    }

    @Test
    void shouldReturnAccessTokenWhenDetailsAreAccessToken() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            SecurityContext mockContext = mock(SecurityContext.class);
            Authentication mockAuthentication = mock(Authentication.class);
            AccessToken mockAccessToken = mock(AccessToken.class);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(mockContext);
            when(mockContext.getAuthentication()).thenReturn(mockAuthentication);
            when(mockAuthentication.getDetails()).thenReturn(mockAccessToken);

            AccessToken accessToken = provider.getAuthenticatedUserInRequest();

            assertNotNull(accessToken, "Expected accessToken to be returned when details are an AccessToken.");
            assertEquals(mockAccessToken, accessToken, "Returned accessToken should match the mockAccessToken.");
        }
    }
}