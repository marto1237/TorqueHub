package torquehub.torquehub.configuration.JWT.token;


public interface AccessToken {

    Long getUserID();

    String getUsername();

    String getRole();

    boolean hasRole(String roleName);
}
