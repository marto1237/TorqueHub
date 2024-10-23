package torquehub.torquehub.configuration.jwt.token;

public interface AccessTokenDecoder {
    AccessToken decode(String accessTokenEncoded);
}
