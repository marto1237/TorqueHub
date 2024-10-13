package torquehub.torquehub.configuration.JWT.token;

import java.util.Set;

public interface AccessToken {
    String getSubject();

    Long getUserID();

    String getRole();

    boolean hasRole(String roleName);
}
