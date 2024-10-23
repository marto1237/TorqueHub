package torquehub.torquehub.business.impl;

import org.springframework.stereotype.Service;
import torquehub.torquehub.business.interfaces.TokenService;
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.configuration.jwt.token.impl.AccessTokenImpl;
import torquehub.torquehub.domain.response.LoginDtos.LoginResponse;

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
}
