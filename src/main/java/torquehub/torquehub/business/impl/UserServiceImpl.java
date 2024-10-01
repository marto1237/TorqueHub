package torquehub.torquehub.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.interfaces.UserService;
import torquehub.torquehub.domain.mapper.UserMapper;
import torquehub.torquehub.domain.model.Role;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.request.LoginDtos.LoginRequest;
import torquehub.torquehub.domain.request.UserDtos.UserCreateRequest;
import torquehub.torquehub.domain.request.UserDtos.UserUpdateRequest;
import torquehub.torquehub.domain.response.LoginDtos.LoginResponse;
import torquehub.torquehub.domain.response.UserDtos.UserResponse;
import torquehub.torquehub.persistence.repository.RoleRepository;
import torquehub.torquehub.persistence.repository.UserRepository;

import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final SecureRandom random = new SecureRandom();

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Override
    public UserResponse createUser(UserCreateRequest userDto) {
        String roleName = (userDto.getRole() == null || userDto.getRole().isEmpty()) ? "USER" : userDto.getRole();
        Optional<Role> roleOptional = roleRepository.findByName(roleName);
        if (roleOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid role: " + roleName);
        }
        Role role = roleOptional.get();

        try {
            // Hash password and generate salt
            String salt = generateSalt();
            String hashedPassword = passwordEncoder.encode(userDto.getPassword() + salt);

            // Create user entity
            User user = User.builder()
                    .username(userDto.getUsername())
                    .email(userDto.getEmail())
                    .password(hashedPassword)
                    .salt(salt)
                    .role(role)
                    .build();

            // Save user
            User savedUser = userRepository.save(user);
            return userMapper.toResponse(savedUser);

        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Username or email already exists.");
        }catch (Exception e) {
            // General error handling
            throw new RuntimeException("Failed to create user. Please try again later.");
        }
    }

    @Override
    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id).map(userMapper::toResponse);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
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
            Optional<User> userOptional = userRepository.findById(id);
            if (userOptional.isPresent()) {
                User existingUser = userOptional.get();
                existingUser.setUsername(userUpdateRequest.getUsername());
                existingUser.setEmail(userUpdateRequest.getEmail());

                userRepository.save(existingUser);
                return true;
            } else {
                throw new IllegalArgumentException("User with ID " + id + " not found.");
            }
        } catch (Exception e) {
            // Log the exception (if needed)
            throw new RuntimeException("Failed to update user: " + e.getMessage());
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
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Check the hashed password with the salt
            String saltedPassword = loginRequest.getPassword() + user.getSalt();
            if (passwordEncoder.matches(saltedPassword, user.getPassword())) {
                return LoginResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole().getName())
                        .build();
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
