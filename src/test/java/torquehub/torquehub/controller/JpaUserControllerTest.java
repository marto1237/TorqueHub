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
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.jwt.token.AccessTokenEncoder;
import torquehub.torquehub.configuration.jwt.token.impl.AccessTokenEncoderDecoderImpl;
import torquehub.torquehub.configuration.jwt.token.impl.BlacklistService;
import torquehub.torquehub.configuration.SecurityConfig;
import torquehub.torquehub.controllers.UserController;
import torquehub.torquehub.domain.request.UserDtos.UserCreateRequest;
import torquehub.torquehub.domain.request.UserDtos.UserUpdateRequest;
import torquehub.torquehub.domain.response.UserDtos.UserResponse;
import torquehub.torquehub.business.interfaces.UserService;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, AccessTokenEncoderDecoderImpl.class, BlacklistService.class})
class JpaUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse testUserResponse;

    @MockBean
    private AccessTokenEncoder accessTokenEncoder;

    @MockBean
    private AccessTokenDecoder accessTokenDecoder;

    @MockBean
    private BlacklistService blacklistService;


    @Test
    @WithMockUser
    void shouldReturnNoUsers_whenGetAllUsers() throws Exception {
        given(userService.getAllUsers()).willReturn(Arrays.asList());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    String role = "USER";

    @BeforeEach
    public void setup() {
        testUserResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
    }

    @Test
    @WithMockUser
    void shouldReturnListOfUsers_whenGetAllUsers() throws Exception {
        given(userService.getAllUsers()).willReturn(Arrays.asList(testUserResponse));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @WithMockUser
    void shouldReturnUser_whenGetUserById() throws Exception {
        given(userService.getUserById(1L)).willReturn(Optional.of(testUserResponse));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("testuser"));
    }


    @Test
    @WithMockUser
    void shouldReturnNotFound_whenGetUserById() throws Exception {
        given(userService.getUserById(1L)).willReturn(Optional.empty());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldCreateUser_whenValidRequest() throws Exception {
        UserCreateRequest newUserRequest = UserCreateRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("newpassword")
                .build();

        UserResponse newUserResponse = UserResponse.builder()
                .id(2L)
                .username("newuser")
                .email("new@example.com")
                .build();

        given(userService.createUser(newUserRequest)).willReturn(newUserResponse);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenCreateUserWithInvalidRole() throws Exception {
        UserCreateRequest invalidUserRequest = UserCreateRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("newpassword")
                .role("INVALID_ROLE") // Invalid role
                .build();

        given(userService.createUser(invalidUserRequest)).willThrow(new IllegalArgumentException("Invalid role"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isConflict()) // Expecting 400 Bad Request
                .andExpect(jsonPath("$.message").value("Invalid role"));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenCreateUserWithInvalidData()  throws Exception {
        UserCreateRequest invalidUserRequest = UserCreateRequest.builder()
                .username("AB") // Invalid : too short min 3 characters max 50 characters
                .email("invlaid-email") // Invalid : invalid email format
                .password("123") // Invalid : too short min 6 characters
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username must be between 3 and 50 characters"))
                .andExpect(jsonPath("$.email").value("Email should be valid"))
                .andExpect(jsonPath("$.password").value("Password must be at least 6 characters"));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenCreateUserWithDuplicateEmailOrUsername() throws Exception {
        UserCreateRequest duplicateUserRequest = UserCreateRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("newpassword")
                .build();

        given(userService.createUser(duplicateUserRequest)).willThrow(new IllegalArgumentException("Username or email already exists."));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUserRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username or email already exists."));
    }

    @Test
    @WithMockUser
    void shouldDeleteUser_whenUserExists() throws Exception {
        given(userService.getUserById(1L)).willReturn(Optional.of(testUserResponse));

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())  // Expecting 200 OK status
                .andExpect(jsonPath("$.message").value("User deleted successfully."));  // Check for the success message

        then(userService).should().deleteUser(1L);
    }

    @Test
    @WithMockUser
    void shouldReturnNotFound_whenDeleteNonExistingUser() throws Exception {
        given(userService.getUserById(999L)).willReturn(Optional.empty());

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound())  // Expecting 404 Not Found status
                .andExpect(jsonPath("$.message").value("User with ID 999 not found."));

        then(userService).should().getUserById(999L);
        then(userService).shouldHaveNoMoreInteractions();
    }


    @Test
    @WithMockUser
    void shouldUpdateUser_whenValidRequest() throws Exception {
        UserUpdateRequest updatedUserRequest = UserUpdateRequest.builder()
                .username("updateduser")
                .email("updated@example.com")
                .build();

        // Mock the service behavior
        given(userService.updateUserById(1L, updatedUserRequest)).willReturn(true);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserRequest)))
                .andExpect(status().isOk())  // Expecting 200 OK
                .andExpect(jsonPath("$.message").value("User updated successfully."));  // Checking for the message

        then(userService).should().updateUserById(1L, updatedUserRequest);
    }

    @Test
    @WithMockUser
    void shouldReturnNotFound_whenUpdateNonExistentUser() throws Exception {
        UserUpdateRequest updatedUserRequest = UserUpdateRequest.builder()
                .username("updateduser")
                .email("update@gmail.com")
                .build();

        given(userService.updateUserById(999L, updatedUserRequest)).willReturn(false);

        mockMvc.perform(put("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User with ID 999 not found."));

    }

    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenUpdateUserWithInvalidData() throws Exception {
        UserUpdateRequest invalidUserRequest = UserUpdateRequest.builder()
                .username("AB") // Invalid : too short min 3 characters max 50 characters
                .email("invlaid-email") // Invalid : invalid email format
                .password("123") // Invalid : too short min 6 characters
                .build();

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username must be between 3 and 50 characters"))
                .andExpect(jsonPath("$.email").value("Email should be valid"))
                .andExpect(jsonPath("$.password").value("Password must be at least 6 characters"));

    }


}
