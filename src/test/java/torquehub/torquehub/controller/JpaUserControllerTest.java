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
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.jwt.token.AccessTokenEncoder;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.configuration.jwt.token.impl.AccessTokenEncoderDecoderImpl;
import torquehub.torquehub.configuration.jwt.token.impl.BlacklistService;
import torquehub.torquehub.configuration.SecurityConfig;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.controllers.UserController;
import torquehub.torquehub.domain.request.user_dtos.UserCreateRequest;
import torquehub.torquehub.domain.request.user_dtos.UserUpdateRequest;
import torquehub.torquehub.domain.request.user_promotion_dtos.UserPromotionRequest;
import torquehub.torquehub.domain.response.user_dtos.UserResponse;
import torquehub.torquehub.domain.response.user_promotion_dtos.UserPromotionResponse;
import torquehub.torquehub.business.interfaces.UserService;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
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

    @MockBean
    private TokenUtil tokenUtil;

    @BeforeEach
    public void setup() {
        testUserResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        AccessToken mockAccessToken = mock(AccessToken.class);
        given(mockAccessToken.getUsername()).willReturn("user");
        given(mockAccessToken.getRole()).willReturn("MODERATOR");

        // Return a valid token when a valid token string is provided
        given(accessTokenDecoder.decode("valid-token")).willReturn(mockAccessToken);
        // Return null when an invalid token string is provided
        given(accessTokenDecoder.decode("invalid-token")).willReturn(null);

        given(tokenUtil.getUserIdFromToken("Bearer valid-token")).willReturn(2L);
    }

    @Test
    @WithMockUser
    void shouldReturnNoUsers_whenGetAllUsers() throws Exception {
        given(userService.getAllUsers()).willReturn(Arrays.asList());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
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
    void shouldReturnConflict_whenCreateUserWithInvalidRole() throws Exception {
        UserCreateRequest invalidUserRequest = UserCreateRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("newpassword")
                .role("INVALID_ROLE")
                .build();

        given(userService.createUser(invalidUserRequest)).willThrow(new IllegalArgumentException("Invalid role"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Invalid role"));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenCreateUserWithInvalidData() throws Exception {
        UserCreateRequest invalidUserRequest = UserCreateRequest.builder()
                .username("AB")
                .email("invalid-email")
                .password("123")
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
    void shouldReturnConflict_whenCreateUserWithDuplicateEmailOrUsername() throws Exception {
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

        mockMvc.perform(delete("/users/1")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully."));

        then(userService).should().deleteUser(1L);
    }

    @Test
    @WithMockUser
    void shouldReturnNotFound_whenDeleteNonExistingUser() throws Exception {
        given(userService.getUserById(999L)).willReturn(Optional.empty());

        mockMvc.perform(delete("/users/999")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound())
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

        given(userService.updateUserById(1L, updatedUserRequest)).willReturn(true);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated successfully."));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFound_whenUpdateNonExistentUser() throws Exception {
        UserUpdateRequest updatedUserRequest = UserUpdateRequest.builder()
                .username("updateduser")
                .email("updated@example.com")
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
                .username("AB")
                .email("invalid-email")
                .build();

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username must be between 3 and 50 characters"))
                .andExpect(jsonPath("$.email").value("Email should be valid"));
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldPromoteUser_whenValidRequest() throws Exception {
        UserPromotionRequest promotionRequest = UserPromotionRequest.builder()
                .promotedUserId(1L)
                .promoterUserId(2L)
                .newRole("ADMIN")
                .build();

        UserPromotionResponse promotionResponse = UserPromotionResponse.builder()
                .promotedUserId(1L)
                .promoterUserId(2L)
                .newRole("ADMIN")
                .build();

        given(userService.promoteUser(promotionRequest)).willReturn(promotionResponse);

        mockMvc.perform(put("/users/1/promote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token")
                        .content(objectMapper.writeValueAsString(promotionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newRole").value("ADMIN"));
    }


    @Test
    @WithMockUser(authorities = {"USER"})
    void shouldReturnUnauthorized_whenPromoteUserWithoutProperRole() throws Exception {
        UserPromotionRequest promotionRequest = UserPromotionRequest.builder()
                .promotedUserId(1L)
                .promoterUserId(2L)
                .newRole("ADMIN")
                .build();

        mockMvc.perform(put("/users/1/promote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer invalid-token")
                        .content(objectMapper.writeValueAsString(promotionRequest)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void shouldReturnBadRequest_whenPromoteUserWithInvalidRole() throws Exception {
        UserPromotionRequest invalidPromotionRequest = UserPromotionRequest.builder()
                .promotedUserId(1L)
                .promoterUserId(2L)
                .newRole("INVALID_ROLE")
                .build();

        given(userService.promoteUser(invalidPromotionRequest)).willThrow(new IllegalArgumentException("Invalid role"));

        mockMvc.perform(put("/users/1/promote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token")
                        .content(objectMapper.writeValueAsString(invalidPromotionRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid role"));
    }

    @Test
    @WithMockUser
    void shouldReturnUnauthorized_whenDeleteUserWithInvalidToken() throws Exception {
        given(userService.getUserById(1L)).willReturn(Optional.of(testUserResponse));

        mockMvc.perform(delete("/users/1")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser
    void shouldReturnInternalServerError_whenGeneralExceptionOccursDuringDeletion() throws Exception {
        given(userService.getUserById(1L)).willReturn(Optional.of(testUserResponse));
        given(userService.deleteUser(1L)).willThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(delete("/users/1")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred: Unexpected error"));
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldReturnInternalServerError_whenGeneralExceptionOccursDuringPromotion() throws Exception {
        UserPromotionRequest promotionRequest = UserPromotionRequest.builder()
                .promotedUserId(1L)
                .promoterUserId(2L)
                .newRole("ADMIN")
                .build();

        given(userService.promoteUser(promotionRequest)).willThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put("/users/1/promote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token")
                        .content(objectMapper.writeValueAsString(promotionRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."));
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldReturnUnauthorized_whenTokenIsInvalidForPromotion() throws Exception {
        UserPromotionRequest promotionRequest = UserPromotionRequest.builder()
                .promotedUserId(1L)
                .promoterUserId(2L)
                .newRole("ADMIN")
                .build();

        given(tokenUtil.getUserIdFromToken("Bearer invalid-token")).willThrow(new InvalidAccessTokenException("Invalid token"));

        mockMvc.perform(put("/users/1/promote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer invalid-token")
                        .content(objectMapper.writeValueAsString(promotionRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenCreateUserWithNullFields() throws Exception {
        UserCreateRequest invalidUserRequest = new UserCreateRequest();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturnInternalServerError_whenGeneralExceptionOccursDuringCreation() throws Exception {
        UserCreateRequest newUserRequest = UserCreateRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("newpassword")
                .build();

        given(userService.createUser(newUserRequest)).willThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred: Unexpected error"));
    }


    @Test
    @WithMockUser
    void shouldReturnInternalServerError_whenGeneralExceptionOccursDuringUpdate() throws Exception {
        UserUpdateRequest updatedUserRequest = UserUpdateRequest.builder()
                .username("updateduser")
                .email("updated@example.com")
                .build();

        given(userService.updateUserById(1L, updatedUserRequest)).willThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred: Unexpected error"));
    }


    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldReturnNotFound_whenPromotedUserDoesNotExist() throws Exception {
        UserPromotionRequest promotionRequest = UserPromotionRequest.builder()
                .promotedUserId(999L)
                .promoterUserId(2L)
                .newRole("ADMIN")
                .build();

        given(userService.promoteUser(promotionRequest)).willThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(put("/users/999/promote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token")
                        .content(objectMapper.writeValueAsString(promotionRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldReturnUnauthorized_whenAccessTokenIsExpired() throws Exception {
        UserPromotionRequest promotionRequest = UserPromotionRequest.builder()
                .promotedUserId(1L)
                .promoterUserId(2L)
                .newRole("ADMIN")
                .build();

        given(tokenUtil.getUserIdFromToken("Bearer expired-token"))
                .willThrow(new InvalidAccessTokenException("Expired token"));

        mockMvc.perform(put("/users/1/promote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer expired-token")
                        .content(objectMapper.writeValueAsString(promotionRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"MODERATOR"})
    void shouldReturnUnauthorized_whenTokenIsInvalidDuringPromotion() throws Exception {
        // Prepare a valid promotion request
        UserPromotionRequest promotionRequest = UserPromotionRequest.builder()
                .promotedUserId(1L)
                .promoterUserId(2L)
                .newRole("ADMIN")
                .build();

        // Simulate the scenario where the token is invalid
        given(tokenUtil.getUserIdFromToken("Bearer invalid-token"))
                .willThrow(new InvalidAccessTokenException("Invalid token"));

        mockMvc.perform(put("/users/1/promote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer invalid-token")
                        .content(objectMapper.writeValueAsString(promotionRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> {
                    // Check if content type is present before asserting
                    if (result.getResponse().getContentType() != null) {
                        assertEquals(MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentType());
                    }
                });
    }



}
