package torquehub.torquehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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
@Import(SecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse testUserResponse;

    @BeforeEach
    public void setup() {
        testUserResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
    }

    @Test
    public void testGetUsers() throws Exception {
        given(userService.getAllUsers()).willReturn(Arrays.asList(testUserResponse));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    public void testGetUserById() throws Exception {
        given(userService.getUserById(1L)).willReturn(Optional.of(testUserResponse));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    public void testCreateUser() throws Exception {
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
    public void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        then(userService).should().deleteUser(1L);
    }

    @Test
    public void testUpdateUser() throws Exception {
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
}
