package torquehub.torquehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import torquehub.torquehub.business.interfaces.TokenService;
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.jwt.token.AccessTokenEncoder;
import torquehub.torquehub.configuration.jwt.token.impl.AccessTokenEncoderDecoderImpl;
import torquehub.torquehub.configuration.jwt.token.impl.BlacklistService;
import torquehub.torquehub.configuration.SecurityConfig;
import torquehub.torquehub.controllers.AuthController;
import torquehub.torquehub.domain.request.LoginDtos.LoginRequest;
import torquehub.torquehub.domain.response.LoginDtos.LoginResponse;
import torquehub.torquehub.business.interfaces.UserService;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    }

    @Test
    @WithMockUser
    void shouldReturnLoginResponse_whenValidCredentials() throws Exception {
        given(userService.login(validLoginRequest)).willReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())  // Expecting 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@email.com"));
    }

    @Test
    void shouldReturnUnauthorized_whenInvalidCredentials() throws Exception {
        LoginRequest invalidLoginRequest = LoginRequest.builder()
                .email("newuser@email.com")
                .password("wrongpassword")  // Wrong password
                .build();

        // Mock: findByEmail returns a user, but password is incorrect
        given(userService.login(invalidLoginRequest)).willThrow(new IllegalArgumentException("Invalid credentials"));

        // Perform the request and check the response
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void shouldReturnBadRequest_whenInvalidEmailFormat() throws Exception {
        // Creating an invalid email login request
        LoginRequest invalidEmailRequest = LoginRequest.builder()
                .email("invalid-email-format")  // Invalid email format
                .password("123456")
                .build();

        // Perform the POST request and expect a 400 Bad Request due to validation failure
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest())  // Expecting 400 Bad Request
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("Email should be valid"));  // Checking the validation message
    }

    @Test
    void shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        LoginRequest nonExistingUserLogin = LoginRequest.builder()
                .email("nonexistent@email.com")
                .password("somepassword")
                .build();


        given(userService.login(nonExistingUserLogin)).willThrow(new IllegalArgumentException("Invalid credentials"));


        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistingUserLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

}
