package torquehub.torquehub.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import torquehub.torquehub.business.interfaces.UserService;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.request.LoginDtos.LoginRequest;
import torquehub.torquehub.domain.request.UserDtos.UserCreateRequest;
import torquehub.torquehub.domain.request.UserDtos.UserUpdateRequest;
import torquehub.torquehub.domain.response.LoginDtos.LoginResponse;
import torquehub.torquehub.domain.response.UserDtos.UserResponse;
import torquehub.torquehub.persistence.repository.UserRepository;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final SecureRandom random = new SecureRandom();

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse createUser(UserCreateRequest userDto) {
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
                    .build();

            // Save user
            User savedUser = userRepository.save(user);
            return mapToResponse(savedUser);

        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                throw new IllegalArgumentException("Username or email already exists.");
            }
            throw e;  // Re-throw other exceptions
        }
    }

    @Override
    public Optional<UserResponse> getUserById(long id) {
        return userRepository.findById(id).map(this::mapToResponse);
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean userExistsById(long id) {
        return userRepository.existsById(id);
    }

    @Override
    public boolean userExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean updateUserById(long id, UserUpdateRequest userUpdateRequest) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            existingUser.setUsername(userUpdateRequest.getUsername());
            existingUser.setEmail(userUpdateRequest.getEmail());
            userRepository.save(existingUser);
            return true;
        }
        return false;
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
                        .build();
            } else {
                throw new IllegalArgumentException("Invalid password.");
            }
        } else {
            throw new IllegalArgumentException("User not found.");
        }
    }


    @Override
    public Optional<UserResponse> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::mapToResponse);
    }

    // Helper method to map User entity to UserResponse DTO
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    @Override
    public Optional<UserResponse> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::mapToResponse);
    }
    private String generateSalt() {
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return new String(saltBytes); // or use a Base64 encoding
    }
}
