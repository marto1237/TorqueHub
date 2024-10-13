package torquehub.torquehub.configuration.JWT.token.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import torquehub.torquehub.configuration.JWT.token.AccessToken;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@EqualsAndHashCode
@Getter
public class AccessTokenImpl implements AccessToken {
    private final String subject;
    private final Long userID;
    private final String role;

    public AccessTokenImpl(String subject, Long userID, String role) {
        this.subject = subject;
        this.userID = userID;
        this.role = role != null ? role : "USER";
    }

    @Override
    public boolean hasRole(String roleName) {
        return this.role.contains(roleName);
    }
}
