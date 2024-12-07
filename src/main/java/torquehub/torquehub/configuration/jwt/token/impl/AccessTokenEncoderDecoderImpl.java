package torquehub.torquehub.configuration.jwt.token.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.jwt.token.AccessTokenEncoder;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AccessTokenEncoderDecoderImpl implements AccessTokenEncoder, AccessTokenDecoder {

    private final Key key;

    public AccessTokenEncoderDecoderImpl(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Key getKey() {
        return key;
    }

    @Override
    public String encode(AccessToken accessToken) {
        long expirationTime = accessToken.getRole() == null ? 7 : 1; // Refresh: 7 days, Access: 1 minute
        Map<String, Object> claimsMap = new HashMap<>();
        if (accessToken.getRole() != null) {
            claimsMap.put("role", accessToken.getRole());
        }
        if (accessToken.getUserID() != null) {
            claimsMap.put("userID", accessToken.getUserID());
        }
        if (accessToken.getUsername() != null) {
            claimsMap.put("username", accessToken.getUsername());
        }

        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(claimsMap)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(expirationTime, ChronoUnit.DAYS)))
                .signWith(key)
                .compact();
    }

    @Override
    public AccessToken decode(String accessTokenEncoded) {
        try {
            Jwt<?, Claims> jwt = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessTokenEncoded);
            Claims claims = jwt.getBody();

            String roles = claims.get("role", String.class);
            Long userID = claims.get("userID", Long.class);
            String username = claims.get("username", String.class);

            return new AccessTokenImpl(username, userID, roles);
        }catch (Exception e){
            throw new InvalidAccessTokenException(e.getMessage());
        }
    }

}
