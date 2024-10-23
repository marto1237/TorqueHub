package torquehub.torquehub.configuration.jwt.token;


public interface AccessToken {

    Long getUserID();

    String getUsername();

    String getRole();

    boolean hasRole(String roleName);
}
