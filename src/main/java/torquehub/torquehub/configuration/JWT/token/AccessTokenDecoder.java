package torquehub.torquehub.configuration.JWT.token;

public interface AccessTokenDecoder {
    AccessToken decode(String accessTokenEncoded);
}
