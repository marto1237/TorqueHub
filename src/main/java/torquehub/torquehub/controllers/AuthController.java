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
import torquehub.torquehub.configuration.jwt.token.impl.BlacklistService;
import torquehub.torquehub.domain.request.LoginDtos.LoginRequest;
import torquehub.torquehub.domain.request.UserDtos.UserCreateRequest;
import torquehub.torquehub.domain.response.LoginDtos.LoginResponse;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.UserDtos.UserResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

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
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginDto,boolean rememberMe) {
        LoginResponse response = userService.login(loginDto);

        if (response != null && response.getId() != null) {
            // If login is successful, generate JWT token
            AccessToken accessToken = tokenService.createAccessToken(response);   // Call method to generate AccessToken
            String jwtToken = accessTokenEncoder.encode(accessToken);  // Encode the AccessToken into JWT

            long cookieMaxAge = rememberMe ? 7 * 24 * 60 * 60 : 24 * 60 * 60; // 1 day or 1 week
            ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", jwtToken)
                    .httpOnly(true)
                    .secure(true) // Use 'true' in production to enable HTTPS
                    .path("/")
                    .maxAge(cookieMaxAge)
                    .sameSite("Strict")  //  CSRF protection
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

            ResponseCookie newJwtCookie = ResponseCookie.from("jwtToken", newAccessToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("Strict")
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
        ResponseCookie logoutCookie = ResponseCookie.from("jwtToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Set expiry to 0 to remove the cookie
                .sameSite("Strict")
                .build();

        response.setMessage("Logged out successfully");

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, logoutCookie.toString())
                .body(response);
    }

    @GetMapping("/auth/check-session")
    public ResponseEntity<UserResponse> checkSession(@CookieValue("jwtToken") String jwtToken) {
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
