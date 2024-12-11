package torquehub.torquehub.business.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.domain.response.login_dtos.LoginResponse;
import torquehub.torquehub.domain.response.user_dtos.UserResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenServiceImplTest {

    private TokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenServiceImpl();
    }

    @Test
    void createAccessTokenFromLoginResponse() {
        // Arrange
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUsername("testuser");
        loginResponse.setId(1L);
        loginResponse.setRole("ROLE_USER");

        // Act
        AccessToken accessToken = tokenService.createAccessToken(loginResponse);

        // Assert
        assertNotNull(accessToken);
        assertEquals("testuser", accessToken.getUsername());
        assertEquals(1L, accessToken.getUserID());
        assertEquals("ROLE_USER", accessToken.getRole());
    }

    @Test
    void createAccessTokenFromUserResponse() {
        // Arrange
        UserResponse userResponse = new UserResponse();
        userResponse.setEmail("test@example.com");
        userResponse.setId(2L);
        userResponse.setRole("ROLE_ADMIN");

        // Act
        AccessToken accessToken = tokenService.createAccessToken(userResponse);

        // Assert
        assertNotNull(accessToken);
        assertEquals("test@example.com", accessToken.getUsername());
        assertEquals(2L, accessToken.getUserID());
        assertEquals("ROLE_ADMIN", accessToken.getRole());
    }

    @Test
    void createRefreshTokenFromLoginResponse() {
        // Arrange
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUsername("refreshuser");
        loginResponse.setId(3L);
        loginResponse.setRole("ROLE_USER");

        // Act
        AccessToken refreshToken = tokenService.createRefreshToken(loginResponse);

        // Assert
        assertNotNull(refreshToken);
        assertEquals("refreshuser", refreshToken.getUsername());
        assertEquals(3L, refreshToken.getUserID());
        assertEquals("ROLE_USER", refreshToken.getRole());
    }

    @Test
    void createAccessTokenFromRefreshToken() {
        // Arrange
        AccessToken mockRefreshToken = mock(AccessToken.class);
        when(mockRefreshToken.getUsername()).thenReturn("refresheduser");
        when(mockRefreshToken.getUserID()).thenReturn(4L);
        when(mockRefreshToken.getRole()).thenReturn("ROLE_MODERATOR");

        // Act
        AccessToken accessToken = tokenService.createAccessToken(mockRefreshToken);

        // Assert
        assertNotNull(accessToken);
        assertEquals("refresheduser", accessToken.getUsername());
        assertEquals(4L, accessToken.getUserID());
        assertEquals("ROLE_MODERATOR", accessToken.getRole());
    }
}
