package torquehub.torquehub.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
import torquehub.torquehub.domain.request.login_dtos.LoginRequest;
import torquehub.torquehub.domain.request.user_dtos.UserCreateRequest;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.login_dtos.LoginResponse;
import torquehub.torquehub.domain.response.user_dtos.UserResponse;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final String SAME_SITE_STRICT = "Strict";
    private static final long ACCESS_TOKEN_EXPIRATION = 300L; // 5 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 86400L; // 1 day

    private final UserService userService;
    private final TokenService tokenService;
    private final AccessTokenDecoder accessTokenDecoder;
    private final AccessTokenEncoder accessTokenEncoder;
    private final BlacklistService blacklistService;

    public AuthController(UserService userService,
                          AccessTokenDecoder accessTokenDecoder,
                          AccessTokenEncoder accessTokenEncoder,
                          TokenService tokenService,
                          BlacklistService blacklistService) {
        this.userService = userService;
        this.accessTokenDecoder = accessTokenDecoder;
        this.accessTokenEncoder = accessTokenEncoder;
        this.tokenService = tokenService;
        this.blacklistService = blacklistService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginDto,
                                               @RequestParam(defaultValue = "false") boolean rememberMe) {
        try {
            LoginResponse response = userService.login(loginDto);
            if (response != null && response.getId() != null) {
                // Generate Access and Refresh Tokens
                AccessToken accessToken = tokenService.createAccessToken(response);
                String accessTokenString = accessTokenEncoder.encode(accessToken);

                AccessToken refreshToken = tokenService.createRefreshToken(response);
                String refreshTokenString = accessTokenEncoder.encode(refreshToken);

                // Create cookies
                ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, accessTokenString)
                        .httpOnly(true)
                        .secure(true) // Enable in production
                        .path("/")
                        .maxAge(ACCESS_TOKEN_EXPIRATION)
                        .sameSite(SAME_SITE_STRICT)
                        .build();

                ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshTokenString)
                        .httpOnly(true)
                        .secure(true) // Enable in production
                        .path("/")
                        .maxAge(REFRESH_TOKEN_EXPIRATION)
                        .sameSite(SAME_SITE_STRICT)
                        .build();

                return ResponseEntity.status(HttpStatus.OK)
                        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                        .body(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        UserResponse createdUser = userService.createUser(userCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<MessageResponse> refreshAccessToken(
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {

        MessageResponse response = new MessageResponse();

        try {
            if (refreshToken == null) {
                response.setMessage("Refresh Token is missing");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Decode and validate the Refresh Token
            AccessToken decodedRefreshToken = accessTokenDecoder.decode(refreshToken);

            if (blacklistService.isTokenBlacklisted(refreshToken)) {
                response.setMessage("Refresh Token is invalid");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Generate a new Access Token
            AccessToken newAccessToken = tokenService.createAccessToken(decodedRefreshToken);
            String newAccessTokenString = accessTokenEncoder.encode(newAccessToken);

            // Set the new Access Token in a cookie
            ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, newAccessTokenString)
                    .httpOnly(true)
                    .secure(true) // Enable in production
                    .path("/")
                    .maxAge(300L) // 5 minutes
                    .sameSite(SAME_SITE_STRICT)
                    .build();

            response.setMessage("Access Token refreshed successfully");
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .body(response);
        } catch (InvalidAccessTokenException e) {
            response.setMessage("Invalid Refresh Token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (RuntimeException e) {
            response.setMessage("An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @CookieValue(value = ACCESS_TOKEN_COOKIE, required = false) String accessToken,
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {

        MessageResponse response = new MessageResponse();

        // Invalidate both tokens
        if (accessToken != null) {
            blacklistService.addTokenToBlacklist(accessToken);
        }
        if (refreshToken != null) {
            blacklistService.addTokenToBlacklist(refreshToken);
        }

        // Clear cookies
        ResponseCookie clearedAccessCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true) // Enable in production
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite(SAME_SITE_STRICT)
                .build();

        ResponseCookie clearedRefreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true) // Enable in production
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite(SAME_SITE_STRICT)
                .build();

        response.setMessage("Logged out successfully");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearedAccessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, clearedRefreshCookie.toString())
                .body(response);
    }

    @GetMapping("/check-session")
    public ResponseEntity<UserResponse> checkSession(
            @CookieValue(value = ACCESS_TOKEN_COOKIE, required = false) String accessToken) {
        try {
            if (accessToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            AccessToken decodedAccessToken = accessTokenDecoder.decode(accessToken);
            UserResponse userResponse = userService.getUserById(decodedAccessToken.getUserID())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
