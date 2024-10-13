package torquehub.torquehub.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import torquehub.torquehub.business.interfaces.TokenService;
import torquehub.torquehub.business.interfaces.UserService;
import torquehub.torquehub.configuration.JWT.token.AccessToken;
import torquehub.torquehub.configuration.JWT.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.JWT.token.AccessTokenEncoder;
import torquehub.torquehub.configuration.JWT.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.configuration.JWT.token.impl.BlacklistService;
import torquehub.torquehub.domain.request.LoginDtos.LoginRequest;
import torquehub.torquehub.domain.request.UserDtos.UserCreateRequest;
import torquehub.torquehub.domain.response.LoginDtos.LoginResponse;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.UserDtos.UserResponse;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;
    private final AccessTokenDecoder accessTokenDecoder;
    private final AccessTokenEncoder accessTokenEncoder;
    private final BlacklistService blacklistService;

    public AuthController(UserService userService,
                          AccessTokenDecoder accessTokenDecoder,
                          AccessTokenEncoder accessTokenEncoder,
                          BlacklistService blacklistService,
                          TokenService tokenService) {
        this.userService = userService;
        this.accessTokenDecoder = accessTokenDecoder;
        this.accessTokenEncoder = accessTokenEncoder;
        this.blacklistService = blacklistService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginDto) {
        LoginResponse response = userService.login(loginDto);

        if (response != null && response.getId() != null) {
            // If login is successful, generate JWT token
            AccessToken accessToken = tokenService.createAccessToken(response);   // Call method to generate AccessToken
            String jwtToken = accessTokenEncoder.encode(accessToken);  // Encode the AccessToken into JWT

            // Add the token to the response object
            response.setJwtToken(jwtToken);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

    }
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        UserResponse createdUser = userService.createUser(userCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshAccessToken(@RequestBody String refreshToken) {
        try {
            AccessToken accessToken = accessTokenDecoder.decode(refreshToken);
            String newAccessToken = accessTokenEncoder.encode(accessToken);
            return ResponseEntity.ok(newAccessToken);
        } catch (InvalidAccessTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Refresh Token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestBody String accessToken) {
        MessageResponse response = new MessageResponse();
        try {
            // Blacklist the token
            blacklistService.addTokenToBlacklist(accessToken);
            response.setMessage("Logout successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("Logout failed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }





}
