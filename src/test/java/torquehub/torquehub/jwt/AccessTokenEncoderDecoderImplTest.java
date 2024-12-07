package torquehub.torquehub.jwt;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.configuration.jwt.token.impl.AccessTokenEncoderDecoderImpl;
import torquehub.torquehub.configuration.jwt.token.impl.AccessTokenImpl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AccessTokenEncoderDecoderImplTest {

    private AccessTokenEncoderDecoderImpl encoderDecoder;

    @Value("${jwt.secret}")
    private String secretKey;

    @BeforeEach
    void setUp() {
        encoderDecoder = new AccessTokenEncoderDecoderImpl(secretKey);
    }

    @Test
    void shouldEncodeAccessTokenWithOneMinuteExpiration() {
        // Arrange
        AccessToken accessToken = new AccessTokenImpl("user123", 1L, "USER");

        // Act
        String token = encoderDecoder.encode(accessToken);
        var claims = Jwts.parserBuilder()
                .setSigningKey(encoderDecoder.getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Assert Claims
        assertThat(claims)
                .containsEntry("username", "user123")
                .containsEntry("role", "USER");

    }



    @Test
    void shouldEncodeRefreshTokenWithSevenDaysExpiration() {
        // Arrange
        AccessToken refreshToken = new AccessTokenImpl(null, 1L, null);

        // Act
        String token = encoderDecoder.encode(refreshToken);
        var claims = Jwts.parserBuilder()
                .setSigningKey(encoderDecoder.getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date expiration = claims.getExpiration();

        // Assert that the expiration time is within a 7-day range
        Instant now = Instant.now();
        Instant expectedExpiration = now.plus(7, ChronoUnit.DAYS);
        Instant bufferStart = now.minusSeconds(5);
        Instant bufferEnd = expectedExpiration.plusSeconds(5);

        assertThat(expiration.toInstant()).isAfterOrEqualTo(bufferStart);
        assertThat(expiration.toInstant()).isBeforeOrEqualTo(bufferEnd);
    }

    @Test
    void shouldDecodeValidAccessToken() {
        // Arrange
        AccessToken accessToken = new AccessTokenImpl("user123", 1L, "USER");
        String token = encoderDecoder.encode(accessToken);

        // Act
        AccessToken decodedToken = encoderDecoder.decode(token);

        // Assert
        assertThat(decodedToken.getUsername()).isEqualTo("user123");
        assertThat(decodedToken.getRole()).isEqualTo("USER");
        assertThat(decodedToken.getUserID()).isEqualTo(1L);
    }

    @Test
    void shouldThrowExceptionWhenDecodingInvalidToken() {
        // Arrange
        String invalidToken = "invalid-token";

        // Act & Assert
        assertThrows(InvalidAccessTokenException.class, () -> encoderDecoder.decode(invalidToken));
    }

    @Test
    void shouldThrowExceptionWhenDecodingExpiredToken() {
        // Arrange
        String expiredToken = Jwts.builder()
                .setSubject("Expired Token")
                .setIssuedAt(Date.from(Instant.now().minusSeconds(3600))) // Token issued an hour ago
                .setExpiration(Date.from(Instant.now().minusSeconds(1800))) // Token expired 30 minutes ago
                .signWith(encoderDecoder.getKey())
                .compact();

        // Act & Assert
        assertThrows(InvalidAccessTokenException.class, () -> encoderDecoder.decode(expiredToken));
    }

    @Test
    void shouldDecodeTokenWithoutRole() {
        // Arrange
        AccessToken refreshToken = new AccessTokenImpl(null, 1L, null);
        String token = encoderDecoder.encode(refreshToken);

        // Act
        AccessToken decodedToken = encoderDecoder.decode(token);

        // Assert
        assertThat(decodedToken.getUsername()).isNull();
        assertThat(decodedToken.getUserID()).isEqualTo(1L);
    }
}
