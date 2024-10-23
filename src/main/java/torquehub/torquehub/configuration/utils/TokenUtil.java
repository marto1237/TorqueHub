package torquehub.torquehub.configuration.utils;

import org.springframework.stereotype.Component;
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;

@Component
public class TokenUtil {

    private final AccessTokenDecoder accessTokenDecoder;

    public TokenUtil(AccessTokenDecoder accessTokenDecoder) {
        this.accessTokenDecoder = accessTokenDecoder;
    }

    public Long getUserIdFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new InvalidAccessTokenException("Invalid or missing token");
        }

        AccessToken accessToken = accessTokenDecoder.decode(token.replace("Bearer ", ""));
        return accessToken.getUserID();
    }
}
