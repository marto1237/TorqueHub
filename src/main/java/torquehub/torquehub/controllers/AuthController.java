package torquehub.torquehub.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.TokenService;
import torquehub.torquehub.business.interfaces.UserService;
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.jwt.token.AccessTokenEncoder;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.domain.request.login_dtos.LoginRequest;
import torquehub.torquehub.domain.request.user_dtos.UserCreateRequest;
import torquehub.torquehub.domain.response.login_dtos.LoginResponse;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.user_dtos.UserResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    // Define constants for repeated literals
    private static final String JWT_TOKEN_COOKIE = "jwtToken";
    private static final String SAME_SITE_STRICT = "Strict";
    private static final long SECONDS_IN_A_DAY = 24 * 60 * 60L;

    private final UserService userService;
    private final TokenService tokenService;
    private final AccessTokenDecoder accessTokenDecoder;
    private final AccessTokenEncoder accessTokenEncoder;

    public AuthController(UserService userService,
                          AccessTokenDecoder accessTokenDecoder,
                          AccessTokenEncoder accessTokenEncoder,
                          TokenService tokenService) {
        this.userService = userService;
        this.accessTokenDecoder = accessTokenDecoder;
        this.accessTokenEncoder = accessTokenEncoder;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginDto,boolean rememberMe) {
        LoginResponse response = userService.login(loginDto);

        if (response != null && response.getId() != null) {
            // If login is successful, generate JWT token
            AccessToken accessToken = tokenService.createAccessToken(response);   // Call method to generate AccessToken
            String jwtToken = accessTokenEncoder.encode(accessToken);  // Encode the AccessToken into JWT

            long cookieMaxAge = rememberMe ? 7 * 24 * 60 * 60 : 24 * 60 * 60; // 1 day or 1 week
            ResponseCookie jwtCookie = ResponseCookie.from(JWT_TOKEN_COOKIE, jwtToken)
                    .httpOnly(true)
                    .secure(true) // Use 'true' in production to enable HTTPS
                    .path("/")
                    .maxAge(cookieMaxAge)
                    .sameSite(SAME_SITE_STRICT)  //  CSRF protection
                    .build();

            return ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(response);
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
    public ResponseEntity<MessageResponse> refreshAccessToken(@RequestBody String refreshToken) {
        MessageResponse response = new MessageResponse();
        try {
            AccessToken accessToken = accessTokenDecoder.decode(refreshToken);
            String newAccessToken = accessTokenEncoder.encode(accessToken);

            ResponseCookie newJwtCookie = ResponseCookie.from(JWT_TOKEN_COOKIE, newAccessToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(SECONDS_IN_A_DAY)
                    .sameSite(SAME_SITE_STRICT)
                    .build();

            response.setMessage("Token refreshed successfully");

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, newJwtCookie.toString())
                    .body(response);
        } catch (InvalidAccessTokenException e) {
            response.setMessage("Invalid Refresh Token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        MessageResponse response = new MessageResponse();


        // Invalidate the JWT cookie by setting a past expiration date
        ResponseCookie logoutCookie = ResponseCookie.from(JWT_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Set expiry to 0 to remove the cookie
                .sameSite(SAME_SITE_STRICT)
                .build();

        response.setMessage("Logged out successfully");

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, logoutCookie.toString())
                .body(response);
    }

    @GetMapping("/auth/check-session")
    public ResponseEntity<UserResponse> checkSession(@CookieValue(JWT_TOKEN_COOKIE) String jwtToken) {
        try {
            AccessToken accessToken = accessTokenDecoder.decode(jwtToken); // Decode and validate JWT
            UserResponse userResponse = userService.getUserById(accessToken.getUserID())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Return 401 if the token is invalid
        }
    }

}
