package torquehub.torquehub.business.impl;

import org.springframework.stereotype.Service;
import torquehub.torquehub.business.interfaces.TokenService;
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.configuration.jwt.token.impl.AccessTokenImpl;
import torquehub.torquehub.domain.response.login_dtos.LoginResponse;
import torquehub.torquehub.domain.response.user_dtos.UserResponse;

@Service
public class TokenServiceImpl implements TokenService {

    @Override
    public AccessToken createAccessToken(LoginResponse loginResponse) {

        // Create AccessToken from loginResponse details
        return new AccessTokenImpl(
                loginResponse.getUsername(),
                loginResponse.getId(),
                loginResponse.getRole()
        );
    }

    @Override
    public AccessToken createAccessToken(UserResponse userResponse) {
        return new AccessTokenImpl(
                userResponse.getEmail(),
                userResponse.getId(),
                userResponse.getRole()
        );
    }

    @Override
    public AccessToken createRefreshToken(LoginResponse loginResponse) {
        return new AccessTokenImpl(
                loginResponse.getUsername(),
                loginResponse.getId(),
                loginResponse.getRole()
        );
    }

    @Override
    public AccessToken createAccessToken(AccessToken refreshToken) {
        // Create a new AccessToken from an existing RefreshToken
        return new AccessTokenImpl(
                refreshToken.getUsername(),
                refreshToken.getUserID(),
                refreshToken.getRole()
        );
    }
}
