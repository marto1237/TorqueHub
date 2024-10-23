package torquehub.torquehub.business.interfaces;

import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.domain.response.LoginDtos.LoginResponse;

public interface TokenService {
    AccessToken createAccessToken(LoginResponse loginResponse);
}
