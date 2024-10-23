package torquehub.torquehub.configuration.jwt.token;

public interface AccessTokenEncoder {
    String encode(AccessToken accessToken);
}
