package torquehub.torquehub.business.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.configuration.utils.TokenUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class TokenUtilTest {
    private AccessTokenDecoder accessTokenDecoder;
    private TokenUtil tokenUtil;

    @BeforeEach
    void setUp() {
        accessTokenDecoder = Mockito.mock(AccessTokenDecoder.class);
        tokenUtil = new TokenUtil(accessTokenDecoder);
    }

    @Test
    void getUserIdFromToken_ValidToken() {
        String token = "Bearer valid_token";
        AccessToken accessToken = Mockito.mock(AccessToken.class);
        when(accessTokenDecoder.decode("valid_token")).thenReturn(accessToken);
        when(accessToken.getUserID()).thenReturn(123L);

        Long userId = tokenUtil.getUserIdFromToken(token);
        assertEquals(123L, userId);
    }

    @Test
    void getUserIdFromToken_InvalidToken() {
        String token = "Invalid token";
        assertThrows(InvalidAccessTokenException.class, () -> tokenUtil.getUserIdFromToken(token));
    }

    @Test
    void getUserIdFromToken_NullToken() {
        assertThrows(InvalidAccessTokenException.class, () -> tokenUtil.getUserIdFromToken(null));
    }
}

