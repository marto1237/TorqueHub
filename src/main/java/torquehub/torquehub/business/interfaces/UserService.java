package torquehub.torquehub.business.interfaces;

import torquehub.torquehub.domain.request.UserCreateRequest;
import torquehub.torquehub.domain.request.UserUpdateRequest;
import torquehub.torquehub.domain.response.UserResponse;

import java.util.List;
import java.util.Optional;

public interface UserService {

    // Return a list of UserResponse instead of List<User>
    List<UserResponse> getAllUsers();

    // Method to create a user and return a UserResponse
    UserResponse createUser(UserCreateRequest userCreateRequest);

    // Return Optional<UserResponse> instead of User
    Optional<UserResponse> getUserById(long id);

    void deleteUser(long id);

    boolean userExistsById(long id);

    boolean userExistsByUsername(String username);

    // Update method that returns boolean
    boolean updateUserById(long id, UserUpdateRequest userUpdateRequest);

    // Find user by username and return Optional<UserResponse>
    Optional<UserResponse> findByUsername(String username);
}
