package torquehub.torquehub.configuration.JWT.token;

public interface AccessTokenEncoder {
    String encode(AccessToken accessToken);
}
