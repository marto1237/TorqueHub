package torquehub.torquehub.business.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import torquehub.torquehub.business.exeption.user_exeptions.UserCreateUserExeption;
import torquehub.torquehub.business.exeption.user_exeptions.UserDeleteExpetion;
import torquehub.torquehub.business.exeption.user_exeptions.UserUpdateExeption;
import torquehub.torquehub.business.interfaces.TokenService;
import torquehub.torquehub.configuration.jwt.token.AccessTokenEncoder;
import torquehub.torquehub.domain.mapper.RoleMapper;
import torquehub.torquehub.domain.mapper.UserMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;
import torquehub.torquehub.domain.model.jpa_models.JpaUserPromotionLog;
import torquehub.torquehub.domain.request.login_dtos.LoginRequest;
import torquehub.torquehub.domain.request.user_dtos.UserCreateRequest;
import torquehub.torquehub.domain.request.user_dtos.UserUpdateRequest;
import torquehub.torquehub.domain.request.user_promotion_dtos.UserPromotionRequest;
import torquehub.torquehub.domain.response.user_dtos.UserResponse;
import torquehub.torquehub.domain.response.user_promotion_dtos.UserPromotionResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaRoleRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaUserPromotionLogRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaUserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private JpaRoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AccessTokenEncoder accessTokenEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JpaUserPromotionLogRepository userPromotionLogRepository;

    private UserCreateRequest userCreateRequest;
    private UserUpdateRequest userUpdateRequest;
    private LoginRequest loginRequest;
    private JpaUser testJpaUser;
    private final String testEmail = "test@email.com";
    private JpaRole userJpaRole;
    private final Long roleId = 1L;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        userJpaRole = JpaRole.builder().name("USER").build();
        String salt = "testSalt";
        String rawPassword = "testpassword";

        // Combine the raw password with the salt and encode it
        String saltedPassword = rawPassword + salt;
        String encodedPassword = passwordEncoder.encode(saltedPassword); // Properly encoded password with salt

        testJpaUser = JpaUser.builder()
                .id(userId)
                .email("test@email.com")
                .username("testUser")
                .password(encodedPassword)  // Store the encoded password here
                .salt(salt)
                .jpaRole(userJpaRole)
                .build();

        userCreateRequest = UserCreateRequest.builder()
                .username("testUser")
                .email(testEmail)
                .password(rawPassword)
                .role("USER")
                .build();

        userUpdateRequest = UserUpdateRequest.builder()
                .username("testUser")
                .email(testEmail)
                .password(rawPassword)
                .build();

        loginRequest = LoginRequest.builder()
                .email(testEmail)
                .password(rawPassword)
                .build();
    }



    @Test
    void shouldReturnUserResponse_WhenUserExistsById() {
        when(userRepository.findById(roleId)).thenReturn(Optional.of(testJpaUser));

        UserResponse mappedResponse = UserResponse.builder()
                .id(testJpaUser.getId())
                .username(testJpaUser.getUsername())
                .email(testJpaUser.getEmail())
                .role(testJpaUser.getJpaRole().getName())
                .build();

        when(userMapper.toResponse(testJpaUser)).thenReturn(mappedResponse);

        Optional<UserResponse> response = userService.getUserById(1L);

        assertTrue(response.isPresent());
        assertEquals("testUser", response.get().getUsername());
        assertEquals("test@email.com", response.get().getEmail());
    }

    @Test
    void shouldReturnEmptyOptional_WhenUserDoesNotExistById() {
        when(userRepository.findById(roleId)).thenReturn(Optional.empty());

        Optional<UserResponse> response = userService.getUserById(roleId);

        assertTrue(response.isEmpty());
    }

    @Test
    void shouldCreateUser_WhenValidUserDetailsProvided() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userJpaRole));
        when(userRepository.save(any(JpaUser.class))).thenReturn(testJpaUser);

        UserResponse mappedResponse = UserResponse.builder()
                .id(testJpaUser.getId())
                .username(testJpaUser.getUsername())
                .email(testJpaUser.getEmail())
                .role(testJpaUser.getJpaRole().getName())
                .build();
        when(userMapper.toResponse(any(JpaUser.class))).thenReturn(mappedResponse);
        UserResponse response = userService.createUser(userCreateRequest);

        assertNotNull(response);
        assertEquals("testUser", response.getUsername());
        assertEquals("test@email.com", response.getEmail());
    }


    @Test
    void shouldThrowException_WhenRoleNotFoundDuringUserCreation() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.createUser(userCreateRequest));

        assertEquals("Invalid role: USER", exception.getMessage());

    }

    @Test
    void shouldThrowException_WhenUsernameOrEmailAlreadyExistsDuringUserCreation() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userJpaRole));
        when(userRepository.save(any(JpaUser.class))).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.createUser(userCreateRequest));

        assertEquals("Username or email already exists.", exception.getMessage());
    }

    @Test
    void shouldReturnEmptyOptional_WhenUserNotFound() {
        when(userRepository.findById(roleId)).thenReturn(Optional.empty());


        Optional<UserResponse> response = userService.getUserById(1L);

        assertTrue(response.isEmpty());
    }

    @Test
    void shouldDeleteUser_WhenUserExists() {
        when(userRepository.findById(roleId)).thenReturn(Optional.of(testJpaUser));

        boolean deleted = userService.deleteUser(roleId);

        assertTrue(deleted);
    }

    @Test
    void shouldReturnTrue_WhenUserExistsById() {
        when(userRepository.existsById(roleId)).thenReturn(true);

        assertTrue(userService.userExistsById(roleId));
    }

    @Test
    void shouldReturnFalse_WhenUserDoesNotExistById() {
        when(userRepository.existsById(roleId)).thenReturn(false);

        boolean result = userService.userExistsById(roleId);

        assertFalse(result);
    }

    @Test
    void shouldReturnTrue_WhenUserExistsByUsername() {
        when(userRepository.existsByUsername("testUser")).thenReturn(true);

        boolean exists = userService.userExistsByUsername("testUser");

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalse_WhenUserDoesNotExistByUsername() {
        when(userRepository.existsByUsername("testUser")).thenReturn(false);

        boolean exists = userService.userExistsByUsername("testUser");

        assertFalse(exists);
    }

    @Test
    void shouldUpdateUser_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testJpaUser));
        when(userRepository.save(any(JpaUser.class))).thenReturn(testJpaUser);

        boolean updated = userService.updateUserById(roleId, userUpdateRequest);

        assertTrue(updated);
    }

    @Test
    void shouldThrowException_WhenUserNotFoundDuringUpdate() {
        when(userRepository.findById(roleId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUserById(1L, userUpdateRequest);
        });

        assertEquals("Failed to update user: User with ID 1 not found.", exception.getMessage());

        verify(userRepository, never()).save(any(JpaUser.class));
    }

    @Test
    void shouldReturnUserResponse_WhenUsernameExists() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testJpaUser));

        UserResponse mappedResponse = UserResponse.builder()
                .id(testJpaUser.getId())
                .username(testJpaUser.getUsername())
                .email(testJpaUser.getEmail())
                .role(testJpaUser.getJpaRole().getName())
                .build();

        when(userMapper.toResponse(testJpaUser)).thenReturn(mappedResponse);

        // Act: Call the service method
        Optional<UserResponse> response = userService.findByUsername("testUser");

        // Assert: Verify the expected results
        assertTrue(response.isPresent());
        assertEquals("testUser", response.get().getUsername());
    }


    @Test
    void shouldReturnEmptyOptional_WhenUsernameDoesNotExist() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        Optional<UserResponse> response = userService.findByUsername("testUser");

        assertTrue(response.isEmpty());
    }

    @Test
    void shouldReturnUserResponse_WhenEmailExists() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testJpaUser));

        UserResponse mappedResponse = UserResponse.builder()
                .id(testJpaUser.getId())
                .username(testJpaUser.getUsername())
                .email(testJpaUser.getEmail())
                .role(testJpaUser.getJpaRole().getName())
                .build();

        when(userMapper.toResponse(testJpaUser)).thenReturn(mappedResponse);
        Optional<UserResponse> response = userService.findByEmail(testEmail);

        assertTrue(response.isPresent());
    }



    @Test
    void shouldThrowException_WhenEmailDoesNotExistDuringLogin() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.login(loginRequest));

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void shouldReturnAllUsers_WhenGetAllUsersCalled() {
        List<JpaUser> userList = Arrays.asList(testJpaUser,
                JpaUser.builder().id(2L).username("user2").build());
        when(userRepository.findAll()).thenReturn(userList);
        when(userMapper.toResponse(any(JpaUser.class)))
                .thenReturn(new UserResponse(1L, "testUser", testEmail, "USER", LocalDateTime.now(), 10));
        List<UserResponse> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
        verify(userMapper, times(2)).toResponse(any(JpaUser.class));
    }

    @Test
    void shouldThrowUserCreateException_WhenUnexpectedErrorOccursDuringUserCreation() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userJpaRole));
        when(userRepository.save(any())).thenThrow(new RuntimeException("Unexpected error"));

        assertThrows(UserCreateUserExeption.class, () ->
                userService.createUser(userCreateRequest));
    }

    @Test
    void shouldThrowUserDeleteException_WhenErrorOccursDuringUserDeletion() {
        when(userRepository.findById(userId)).thenThrow(new RuntimeException("Database error"));

        assertThrows(UserDeleteExpetion.class, () ->
                userService.deleteUser(userId));
    }


    @Test
    void shouldThrowUserUpdateException_WhenUnexpectedErrorOccursDuringUserUpdate() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testJpaUser));
        when(userRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        assertThrows(UserUpdateExeption.class, () ->
                userService.updateUserById(userId, userUpdateRequest));
    }

    @Test
    void shouldUseDefaultRole_WhenRoleIsNullDuringUserCreation() {
        UserCreateRequest requestWithoutRole = UserCreateRequest.builder()
                .username("testUser")
                .email(testEmail)
                .password("testpassword")
                .build();

        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userJpaRole));
        when(userRepository.save(any(JpaUser.class))).thenReturn(testJpaUser);
        when(userMapper.toResponse(any(JpaUser.class)))
                .thenReturn(new UserResponse(1L, "testUser", testEmail, "USER",LocalDateTime.now(), 10));

        UserResponse response = userService.createUser(requestWithoutRole);

        assertNotNull(response);
        assertEquals("USER", response.getRole());
    }



    @Test
    void shouldThrowException_WhenPasswordIsInvalidDuringLogin() {
        // Mock the user repository to return a test user
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testJpaUser));

        lenient().when(passwordEncoder.matches(anyString(), eq(testJpaUser.getPassword()))).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.login(loginRequest));

        assertEquals("Invalid credentials", exception.getMessage());
    }


    @Test
    void shouldPromoteUser_WhenValidPromotionRequest() {
        // Create the promotion request and expected role
        UserPromotionRequest promotionRequest = new UserPromotionRequest(1L, 2L, "ADMIN", LocalDateTime.now());
        JpaRole adminRole = JpaRole.builder().name("ADMIN").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testJpaUser));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(JpaUser.class))).thenReturn(testJpaUser);
        when(userPromotionLogRepository.save(any(JpaUserPromotionLog.class))).thenReturn(mock(JpaUserPromotionLog.class));

        UserPromotionResponse response = userService.promoteUser(promotionRequest);

        assertNotNull(response);
        assertEquals(1L, response.getPromotedUserId());
        assertEquals(2L, response.getPromoterUserId());
        assertEquals("ADMIN", response.getNewRole());
        assertNotNull(response.getTimestamp());
        assertEquals("User promoted successfully.", response.getMessage());
    }


    @Test
    void shouldThrowException_WhenPromotedUserNotFound() {
        UserPromotionRequest promotionRequest = new UserPromotionRequest(1L, 2L, "ADMIN", LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.promoteUser(promotionRequest));

        assertEquals("User with ID 1 not found.", exception.getMessage());
    }


    @Test
    void shouldThrowException_WhenPromotionRoleInvalid() {
        UserPromotionRequest promotionRequest = new UserPromotionRequest(1L, 2L, "INVALID_ROLE", LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(testJpaUser));
        when(roleRepository.findByName("INVALID_ROLE")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.promoteUser(promotionRequest));

        assertEquals("Invalid role: INVALID_ROLE", exception.getMessage());
    }


    // Test generateSalt method indirectly through user creation
    @Test
    void shouldGenerateUniqueSalt_WhenCreatingUser() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userJpaRole));
        when(userRepository.save(any(JpaUser.class))).thenAnswer(invocation -> {
            JpaUser savedUser = invocation.getArgument(0);
            assertNotNull(savedUser.getSalt());
            assertTrue(savedUser.getSalt().length() > 0);
            return savedUser;
        });
        when(userMapper.toResponse(any(JpaUser.class)))
                .thenReturn(new UserResponse(1L, "testUser", testEmail, "USER",LocalDateTime.now(), 10));

        userService.createUser(userCreateRequest);

        verify(userRepository).save(argThat(user ->
                user.getSalt() != null && !user.getSalt().isEmpty()
        ));
    }

    @Test
    void shouldThrowException_WhenUserDeletedDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserDeleteExpetion exception = assertThrows(UserDeleteExpetion.class,
                () -> userService.deleteUser(userId));

        assertEquals("Failed to delete user: User with ID 1 not found.", exception.getMessage());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void shouldReturnEmptyOptional_WhenEmailDoesNotExist() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        Optional<UserResponse> response = userService.findByEmail(testEmail);

        assertTrue(response.isEmpty());
        verify(userMapper, never()).toResponse(any());
    }
}
