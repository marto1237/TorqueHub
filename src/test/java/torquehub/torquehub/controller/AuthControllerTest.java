package torquehub.torquehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import torquehub.torquehub.business.interfaces.TokenService;
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.jwt.token.AccessTokenEncoder;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.configuration.jwt.token.impl.AccessTokenEncoderDecoderImpl;
import torquehub.torquehub.configuration.jwt.token.impl.BlacklistService;
import torquehub.torquehub.configuration.SecurityConfig;
import torquehub.torquehub.controllers.AuthController;
import torquehub.torquehub.domain.request.login_dtos.LoginRequest;
import torquehub.torquehub.domain.request.user_dtos.UserCreateRequest;
import torquehub.torquehub.domain.response.login_dtos.LoginResponse;
import torquehub.torquehub.business.interfaces.UserService;
import torquehub.torquehub.domain.response.user_dtos.UserResponse;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AccessTokenEncoderDecoderImpl.class, BlacklistService.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccessTokenEncoder accessTokenEncoder;

    @MockBean
    private AccessTokenDecoder accessTokenDecoder;

    @MockBean
    private BlacklistService blacklistService;

    @MockBean
    private TokenService tokenService;

    private LoginRequest validLoginRequest;
    private LoginResponse loginResponse;
    private UserCreateRequest validUserCreateRequest;
    private UserResponse userResponse;

    @BeforeEach
    public void setup() {
        validLoginRequest = LoginRequest.builder()
                .email("newuser@email.com")
                .password("newuser")
                .build();

        loginResponse = LoginResponse.builder()
                .id(1L)
                .username("newuser")
                .email("newuser@email.com")
                .build();

        validUserCreateRequest = UserCreateRequest.builder()
                .username("newuser")
                .email("newuser@email.com")
                .password("password123")
                .role("USER")
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .username("newuser")
                .email("newuser@email.com")
                .role("USER")
                .points(100)
                .build();
    }

    @Test
    @WithMockUser
    void shouldReturnLoginResponse_whenValidCredentials() throws Exception {
        given(userService.login(validLoginRequest)).willReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                        .param("rememberMe", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@email.com"));
    }

    @Test
    @WithMockUser
    void shouldReturnUnauthorized_whenInvalidCredentials() throws Exception {
        LoginRequest invalidLoginRequest = LoginRequest.builder()
                .email("newuser@email.com")
                .password("wrongpassword")
                .build();

        given(userService.login(invalidLoginRequest)).willThrow(new IllegalArgumentException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .param("rememberMe", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnBadRequest_whenInvalidEmailFormat() throws Exception {
        LoginRequest invalidEmailRequest = LoginRequest.builder()
                .email("invalid-email-format")
                .password("123456")
                .build();

        mockMvc.perform(post("/auth/login")
                        .param("rememberMe", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest());
    }

    // Tests for the /register endpoint
    @Test
    void shouldRegisterUser_whenValidInput() throws Exception {
        given(userService.createUser(validUserCreateRequest)).willReturn(userResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@email.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.points").value(100));
    }

    @Test
    void shouldReturnBadRequest_whenInvalidRegisterInput() throws Exception {
        UserCreateRequest invalidUserCreateRequest = UserCreateRequest.builder()
                .username("")
                .email("invalid-email")
                .password("123")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    // Tests for the /refresh-token endpoint
    @Test
    @WithMockUser
    void shouldReturnNewToken_whenValidRefreshToken() throws Exception {
        String validRefreshToken = "valid-refresh-token";
        AccessToken decodedRefreshToken = createMockAccessToken();

        given(accessTokenDecoder.decode(validRefreshToken)).willReturn(decodedRefreshToken);
        given(blacklistService.isTokenBlacklisted(validRefreshToken)).willReturn(false);
        given(tokenService.createAccessToken(decodedRefreshToken)).willReturn(decodedRefreshToken);
        given(accessTokenEncoder.encode(decodedRefreshToken)).willReturn("new-access-token");

        mockMvc.perform(post("/auth/refresh-token")
                        .cookie(new MockCookie("refreshToken", validRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.message").value("Access Token refreshed successfully"));
    }


    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenRefreshTokenCookieIsMissing() throws Exception {
        mockMvc.perform(post("/auth/refresh-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Refresh Token is missing"));
    }



    @Test
    void shouldReturnUnauthorized_whenRefreshTokenIsInvalid() throws Exception {
        String invalidToken = "invalid-refresh-token";

        given(accessTokenDecoder.decode(invalidToken)).willThrow(new InvalidAccessTokenException("Invalid Refresh Token"));

        mockMvc.perform(post("/auth/refresh-token")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    // Tests for the /check-session endpoint
    @Test
    @WithMockUser
    void shouldReturnUserDetails_whenSessionIsValid() throws Exception {
        String validAccessToken = "valid-access-token";
        AccessToken decodedAccessToken = createMockAccessToken();

        given(accessTokenDecoder.decode(validAccessToken)).willReturn(decodedAccessToken);
        given(userService.getUserById(1L)).willReturn(Optional.of(userResponse));

        mockMvc.perform(get("/auth/check-session")
                        .cookie(new MockCookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.points").value(100));
    }


    @Test
    @WithMockUser
    void shouldReturnUnauthorized_whenSessionIsInvalid() throws Exception {
        String invalidToken = "invalid-token";

        given(accessTokenDecoder.decode(invalidToken)).willThrow(new InvalidAccessTokenException("Invalid JWT Token"));

        mockMvc.perform(get("/auth/check-session")
                        .cookie(new MockCookie("jwtToken", invalidToken)))
                .andExpect(status().isUnauthorized());
    }

    private AccessToken createMockAccessToken() {
        return new AccessToken() {
            @Override
            public Long getUserID() {
                return 1L;
            }

            @Override
            public String getUsername() {
                return "newuser";
            }

            @Override
            public String getRole() {
                return "USER";
            }

            @Override
            public boolean hasRole(String roleName) {
                return "USER".equals(roleName);
            }
        };
    }

    @Test
    @WithMockUser
    void shouldReturnUnauthorized_whenUnexpectedExceptionOccursDuringLogin() throws Exception {
        given(userService.login(validLoginRequest)).willThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/auth/login")
                        .param("rememberMe", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser
    void shouldInvalidateCookieOnLogout() throws Exception {
        // Mock the decoding of a valid token if needed by the security filter
        AccessToken mockAccessToken = createMockAccessToken();
        given(accessTokenDecoder.decode("valid_token")).willReturn(mockAccessToken);

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer valid_token") // Add Authorization header
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    @WithMockUser
    void shouldLogoutSuccessfully_andInvalidateCookie() throws Exception {
        AccessToken mockAccessToken = createMockAccessToken();
        given(accessTokenDecoder.decode("valid_token")).willReturn(mockAccessToken);

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer valid_token") // Add Authorization header
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0"))) // Verify cookie invalidation
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }


    @Test
    @WithMockUser
    void shouldReturnUnauthorized_whenUnexpectedErrorOccursDuringLogin() throws Exception {
        given(userService.login(validLoginRequest)).willThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void shouldReturnUnauthorized_whenLoginServiceReturnsNull() throws Exception {
        given(userService.login(validLoginRequest)).willReturn(null);

        mockMvc.perform(post("/auth/login")
                        .param("rememberMe", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void shouldReturnUnauthorized_whenInvalidAccessTokenExceptionOccurs() throws Exception {
        String invalidToken = "invalid-refresh-token";
        given(accessTokenDecoder.decode(invalidToken)).willThrow(new InvalidAccessTokenException("Invalid Refresh Token"));

        mockMvc.perform(post("/auth/refresh-token")
                        .header("Authorization", "Bearer " + invalidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser
    void shouldReturnUnauthorized_whenAccessTokenIsMissing() throws Exception {
        mockMvc.perform(get("/auth/check-session"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void shouldReturnUnauthorized_whenAccessTokenIsInvalid() throws Exception {
        String invalidAccessToken = "invalid-access-token";

        given(accessTokenDecoder.decode(invalidAccessToken)).willThrow(new InvalidAccessTokenException("Invalid JWT Token"));

        mockMvc.perform(get("/auth/check-session")
                        .cookie(new MockCookie("accessToken", invalidAccessToken)))
                .andExpect(status().isUnauthorized());
    }



}
