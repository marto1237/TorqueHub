package torquehub.torquehub.configuration.JWT.token;


public interface AccessToken {
    String getSubject();

    Long getUserID();

    String getRole();

    boolean hasRole(String roleName);
}
