package torquehub.torquehub.business.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import torquehub.torquehub.domain.mapper.RoleMapper;
import torquehub.torquehub.domain.mapper.UserMapper;
import torquehub.torquehub.domain.model.Role;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.request.LoginDtos.LoginRequest;
import torquehub.torquehub.domain.request.UserDtos.UserCreateRequest;
import torquehub.torquehub.domain.request.UserDtos.UserUpdateRequest;
import torquehub.torquehub.domain.response.UserDtos.UserResponse;
import torquehub.torquehub.persistence.repository.RoleRepository;
import torquehub.torquehub.persistence.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private UserCreateRequest userCreateRequest;
    private UserUpdateRequest userUpdateRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private String testemail = "test@email.com";
    private Role userRole;
    private final Long roleId = 1L;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().name("USER").build();

        testUser = User.builder()
                .id(roleId)
                .email("test@email.com")
                .username("testUser")
                .password("hashedPassword")
                .salt("testSalt")
                .role(userRole)
                .build();

        userCreateRequest = UserCreateRequest.builder()
                .username("testUser")
                .email("test@email.com")
                .password("testpassword")
                .role("USER")
                .build();

        userUpdateRequest = UserUpdateRequest.builder()
                .username("testUser")
                .email("test@email.com")
                .password("testpassword")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@email.com")
                .password("testpassword")
                .build();
    }



    @Test
    void shouldReturnUserById_whenUserExists() {
        when(userRepository.findById(roleId)).thenReturn(Optional.of(testUser));

        UserResponse mappedResponse = UserResponse.builder()
                .id(testUser.getId())
                .username(testUser.getUsername())
                .email(testUser.getEmail())
                .role(testUser.getRole().getName())
                .build();

        when(userMapper.toResponse(testUser)).thenReturn(mappedResponse);

        Optional<UserResponse> response = userService.getUserById(1L);

        assertTrue(response.isPresent());
        assertEquals("testUser", response.get().getUsername());
        assertEquals("test@email.com", response.get().getEmail());
    }

    @Test
    void shouldReturnEmpty_whenUserDoesNotExist() {
        when(userRepository.findById(roleId)).thenReturn(Optional.empty());

        Optional<UserResponse> response = userService.getUserById(roleId);

        assertTrue(response.isEmpty());
    }

    @Test
    void shouldCreateUserSuccessfully_whenValidUserDetailsProvided() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse mappedResponse = UserResponse.builder()
                .id(testUser.getId())
                .username(testUser.getUsername())
                .email(testUser.getEmail())
                .role(testUser.getRole().getName())
                .build();
        when(userMapper.toResponse(any(User.class))).thenReturn(mappedResponse);
        UserResponse response = userService.createUser(userCreateRequest);

        assertNotNull(response);
        assertEquals("testUser", response.getUsername());
        assertEquals("test@email.com", response.getEmail());
    }


    @Test
    void shouldThrowException_whenRoleNotFoundWhileCreatingUser() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.createUser(userCreateRequest));

        assertEquals("Invalid role: USER", exception.getMessage());

    }

    @Test
    void shouldThrowException_whenUsernameOrEmailAlreadyExists() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.createUser(userCreateRequest));

        assertEquals("Username or email already exists.", exception.getMessage());
    }

    @Test
    void shouldReturnEmptyOptional_whenUserNotFoundById() {
        when(userRepository.findById(roleId)).thenReturn(Optional.empty());


        Optional<UserResponse> response = userService.getUserById(1L);

        assertTrue(response.isEmpty());
    }

    @Test
    void shouldDeleteUserSuccessfully_whenUserExists() {
        when(userRepository.findById(roleId)).thenReturn(Optional.of(testUser));

        boolean deleted = userService.deleteUser(roleId);

        assertTrue(deleted);
    }

    @Test
    void shouldReturnTrue_whenUserExistsById() {
        when(userRepository.existsById(roleId)).thenReturn(true);

        assertTrue(userService.userExistsById(roleId));
    }

    @Test
    void shouldReturnFalse_whenUserDoesNotExistById() {
        when(userRepository.existsById(roleId)).thenReturn(false);

        boolean result = userService.userExistsById(roleId);

        assertFalse(result);
    }

    @Test
    void shouldReturnTrue_whenUserExistsByUsername() {
        when(userRepository.existsByUsername("testUser")).thenReturn(true);

        boolean exists = userService.userExistsByUsername("testUser");

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalse_whenUserDoesNotExistByUsername() {
        when(userRepository.existsByUsername("testUser")).thenReturn(false);

        boolean exists = userService.userExistsByUsername("testUser");

        assertFalse(exists);
    }

    @Test
    void shouldUpdateUserSuccessfully_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        boolean updated = userService.updateUserById(roleId, userUpdateRequest);

        assertTrue(updated);
    }

    @Test
    void shouldNotUpdateUser_whenUserDoesNotExist() {
        when(userRepository.findById(roleId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUserById(1L, userUpdateRequest);
        });

        assertEquals("Failed to update user: User with ID 1 not found.", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldReturnUser_whenUsernameExists() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        UserResponse mappedResponse = UserResponse.builder()
                .id(testUser.getId())
                .username(testUser.getUsername())
                .email(testUser.getEmail())
                .role(testUser.getRole().getName())
                .build();

        when(userMapper.toResponse(testUser)).thenReturn(mappedResponse);

        // Act: Call the service method
        Optional<UserResponse> response = userService.findByUsername("testUser");

        // Assert: Verify the expected results
        assertTrue(response.isPresent());
        assertEquals("testUser", response.get().getUsername());
    }


    @Test
    void shouldReturnEmpty_whenUsernameDoesNotExist() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        Optional<UserResponse> response = userService.findByUsername("testUser");

        assertTrue(response.isEmpty());
    }

    @Test
    void shouldReturnUser_whenEmailExists() {
        when(userRepository.findByEmail(testemail)).thenReturn(Optional.of(testUser));

        UserResponse mappedResponse = UserResponse.builder()
                .id(testUser.getId())
                .username(testUser.getUsername())
                .email(testUser.getEmail())
                .role(testUser.getRole().getName())
                .build();

        when(userMapper.toResponse(testUser)).thenReturn(mappedResponse);
        Optional<UserResponse> response = userService.findByEmail(testemail);

        assertTrue(response.isPresent());
    }



    @Test
    void shouldThrowException_whenEmailDoesNotExist() {
        when(userRepository.findByEmail(testemail)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.login(loginRequest));

        assertEquals("Invalid credentials", exception.getMessage());
    }




}
