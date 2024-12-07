package torquehub.torquehub.business.interfaces;

import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.domain.response.login_dtos.LoginResponse;
import torquehub.torquehub.domain.response.user_dtos.UserResponse;

public interface TokenService {
    AccessToken createAccessToken(LoginResponse loginResponse);

    AccessToken createAccessToken(UserResponse userResponse);

    AccessToken createRefreshToken(LoginResponse loginResponse);

    AccessToken createAccessToken(AccessToken refreshToken);
}
