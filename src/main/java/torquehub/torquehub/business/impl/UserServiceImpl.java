    package torquehub.torquehub.business.impl;

    import org.springframework.dao.DataIntegrityViolationException;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import torquehub.torquehub.business.exeption.user_exeptions.UserCreateUserExeption;
    import torquehub.torquehub.business.exeption.user_exeptions.UserDeleteExpetion;
    import torquehub.torquehub.business.exeption.user_exeptions.UserUpdateExeption;
    import torquehub.torquehub.business.interfaces.TokenService;
    import torquehub.torquehub.business.interfaces.UserService;
    import torquehub.torquehub.configuration.jwt.token.AccessToken;
    import torquehub.torquehub.configuration.jwt.token.AccessTokenEncoder;
    import torquehub.torquehub.domain.mapper.UserMapper;
    import torquehub.torquehub.domain.model.jpa_models.JpaUser;
    import torquehub.torquehub.domain.model.jpa_models.JpaRole;
    import torquehub.torquehub.domain.model.jpa_models.JpaUserPromotionLog;
    import torquehub.torquehub.domain.request.login_dtos.LoginRequest;
    import torquehub.torquehub.domain.request.user_dtos.UserCreateRequest;
    import torquehub.torquehub.domain.request.user_dtos.UserUpdateRequest;
    import torquehub.torquehub.domain.request.user_promotion_dtos.UserPromotionRequest;
    import torquehub.torquehub.domain.response.login_dtos.LoginResponse;
    import torquehub.torquehub.domain.response.user_dtos.UserResponse;
    import torquehub.torquehub.domain.response.user_promotion_dtos.UserPromotionResponse;
    import torquehub.torquehub.persistence.jpa.impl.JpaRoleRepository;
    import torquehub.torquehub.persistence.jpa.impl.JpaUserPromotionLogRepository;
    import torquehub.torquehub.persistence.jpa.impl.JpaUserRepository;

    import java.security.SecureRandom;
    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.Optional;

    @Service
    public class UserServiceImpl implements UserService {

        private final JpaUserRepository userRepository;
        private final JpaRoleRepository roleRepository;
        private final UserMapper userMapper;
        private final TokenService tokenService;
        private final AccessTokenEncoder accessTokenEncoder;
        private final JpaUserPromotionLogRepository userPromotionLogRepository;

        public UserServiceImpl(JpaUserRepository userRepository,
                               JpaRoleRepository roleRepository,
                               UserMapper userMapper,
                               TokenService tokenService,
                               AccessTokenEncoder accessTokenEncoder,
                               JpaUserPromotionLogRepository userPromotionLogRepository) {
            this.userRepository = userRepository;
            this.roleRepository = roleRepository;
            this.userMapper = userMapper;
            this.tokenService = tokenService;
            this.accessTokenEncoder = accessTokenEncoder;
            this.userPromotionLogRepository = userPromotionLogRepository;
        }

        private static final String USER_ID_NOT_FOUND = "User with ID ";
        private static final String NOT_FOUND = " not found.";
        private static final String INVALID_ROLE = "Invalid role: ";
        private static final String DUPLICATE_USERNAME_OR_EMAIL = "Username or email already exists.";
        private static final String FAILED_TO_CREATE_USER = "Failed to create user. Please try again later.";

        private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        private final SecureRandom random = new SecureRandom();

        @Override
        public List<UserResponse> getAllUsers() {
            return userRepository.findAll().stream()
                    .map(userMapper::toResponse)
                    .toList();
        }


        @Override
        @Transactional
        public UserResponse createUser(UserCreateRequest userDto) {
            String roleName = (userDto.getRole() == null || userDto.getRole().isEmpty()) ? "USER" : userDto.getRole();
            Optional<JpaRole> roleOptional = roleRepository.findByName(roleName);
            if (roleOptional.isEmpty()) {
                throw new IllegalArgumentException(INVALID_ROLE + roleName);
            }
            JpaRole jpaRole = roleOptional.get();

            try {
                // Hash password and generate salt
                String salt = generateSalt();
                String hashedPassword = passwordEncoder.encode(userDto.getPassword() + salt);

                // Create user entity
                JpaUser jpaUser = JpaUser.builder()
                        .username(userDto.getUsername())
                        .email(userDto.getEmail())
                        .password(hashedPassword)
                        .salt(salt)
                        .jpaRole(jpaRole)
                        .build();

                // Save user
                JpaUser savedJpaUser = userRepository.save(jpaUser);
                return userMapper.toResponse(savedJpaUser);

            } catch (DataIntegrityViolationException e) {
                throw new IllegalArgumentException(DUPLICATE_USERNAME_OR_EMAIL);
            }catch (Exception e) {
                throw new UserCreateUserExeption(FAILED_TO_CREATE_USER,e);
            }
        }

        @Override
        public Optional<UserResponse> getUserById(Long id) {
            return userRepository.findById(id).map(userMapper::toResponse);
        }

        @Override
        @Transactional
        public boolean deleteUser(Long id) {
            try {
                Optional<JpaUser> userOptional = userRepository.findById(id);
                if(userOptional.isPresent()){
                    JpaUser jpaUser = userOptional.get();
                    userRepository.delete(jpaUser);
                    return true;
                }else {
                    throw new IllegalArgumentException(USER_ID_NOT_FOUND + id + NOT_FOUND);
                }
            } catch (Exception e) {
                throw new UserDeleteExpetion("Failed to delete user: " + e.getMessage(),e);
            }
        }

        @Override
        public boolean userExistsById(Long id) {
            return userRepository.existsById(id);
        }

        @Override
        public boolean userExistsByUsername(String username) {
            return userRepository.existsByUsername(username);
        }

        @Override
        @Transactional
        public boolean updateUserById(Long id, UserUpdateRequest userUpdateRequest) {
            try {
                Optional<JpaUser> userOptional = userRepository.findById(id);
                if (userOptional.isPresent()) {
                    JpaUser existingJpaUser = userOptional.get();
                    existingJpaUser.setUsername(userUpdateRequest.getUsername());
                    existingJpaUser.setEmail(userUpdateRequest.getEmail());

                    userRepository.save(existingJpaUser);
                    return true;
                } else {
                    throw new IllegalArgumentException(USER_ID_NOT_FOUND + id + NOT_FOUND);
                }
            } catch (Exception e) {
                // Log the exception (if needed)
                throw new UserUpdateExeption("Failed to update user: " + e.getMessage(),e);
            }
        }

        @Override
        @Transactional
        public UserPromotionResponse promoteUser(UserPromotionRequest request) {
            Long promoterId = request.getPromoterUserId();
            Long promotedUserId = request.getPromotedUserId();
            String newRole = request.getNewRole();

            Optional<JpaUser> userOptional = userRepository.findById(promotedUserId);
            if (userOptional.isPresent()) {
                JpaUser user = userOptional.get();
                Optional<JpaRole> roleOptional = roleRepository.findByName(newRole);
                if (roleOptional.isPresent()) {
                    user.setJpaRole(roleOptional.get());
                    userRepository.save(user);

                    // Log the promotion
                    JpaUserPromotionLog promotionLog = JpaUserPromotionLog.builder()
                            .promotedUserId(promotedUserId)
                            .promoterUserId(promoterId)
                            .newRole(newRole)
                            .timestamp(LocalDateTime.now())
                            .build();
                    userPromotionLogRepository.save(promotionLog);

                    // Create and return the UserPromotionResponse using the builder pattern
                    return UserPromotionResponse.builder()
                            .promotedUserId(promotedUserId)
                            .promoterUserId(promoterId)
                            .newRole(newRole)
                            .timestamp(promotionLog.getTimestamp())
                            .message("User promoted successfully.")
                            .build();
                } else {
                    throw new IllegalArgumentException(INVALID_ROLE + newRole);
                }
            } else {
                throw new IllegalArgumentException(USER_ID_NOT_FOUND + promotedUserId + NOT_FOUND);
            }
        }


        @Override
        public Optional<UserResponse> findByUsername(String username) {
            return userRepository.findByUsername(username).map(userMapper::toResponse);
        }


        @Override
        public Optional<UserResponse> findByEmail(String email) {
            return userRepository.findByEmail(email).map(userMapper::toResponse);
        }
        private String generateSalt() {
            byte[] saltBytes = new byte[16];
            random.nextBytes(saltBytes);
            return new String(saltBytes); // or use a Base64 encoding
        }

        @Override
        public LoginResponse login(LoginRequest loginRequest) {
            Optional<JpaUser> userOptional = userRepository.findByEmail(loginRequest.getEmail());

            if (userOptional.isPresent()) {
                JpaUser jpaUser = userOptional.get();

                // Check the hashed password with the salt
                String saltedPassword = loginRequest.getPassword() + jpaUser.getSalt();
                if (passwordEncoder.matches(saltedPassword, jpaUser.getPassword())) {
                    LoginResponse loginResponse = LoginResponse.builder()
                            .id(jpaUser.getId())
                            .username(jpaUser.getUsername())
                            .email(jpaUser.getEmail())
                            .role(jpaUser.getJpaRole().getName())
                            .build();

                    // Generate AccessToken using TokenService
                    AccessToken accessToken = tokenService.createAccessToken(loginResponse);
                    String jwtToken = accessTokenEncoder.encode(accessToken);

                    // Add the JWT token to the response
                    loginResponse.setJwtToken(jwtToken);

                    return loginResponse;
                } else {
                    // Generic error message: Invalid credentials (even though the email is valid)
                    throw new IllegalArgumentException("Invalid credentials");
                }
            } else {
                // Generic error message: Invalid credentials (email not found)
                throw new IllegalArgumentException("Invalid credentials");
            }
        }
    }
