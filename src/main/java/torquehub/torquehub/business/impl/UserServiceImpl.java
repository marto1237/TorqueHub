package torquehub.torquehub.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import torquehub.torquehub.business.interfaces.UserService;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.request.UserCreateRequest;
import torquehub.torquehub.domain.request.UserUpdateRequest;
import torquehub.torquehub.domain.response.UserResponse;
import torquehub.torquehub.persistence.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public List<UserResponse> getAllUsers() {
        // Fetch users and map to UserResponse DTO
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse createUser(UserCreateRequest userDto) {
        // Hash the password
        String hashedPassword = passwordEncoder.encode(userDto.getPassword());

        // Create user entity
        User user = User.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .password(hashedPassword)
                .build();

        // Save the user
        User savedUser = userRepository.save(user);

        // Return the response DTO
        return mapToResponse(savedUser);
    }

    @Override
    public Optional<UserResponse> getUserById(long id) {
        // Fetch the user by ID and map to response DTO
        return userRepository.findById(id)
                .map(this::mapToResponse);
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
    public Optional<UserResponse> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::mapToResponse);
    }

    // Helper method to map User entity to UserResponse DTO
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
