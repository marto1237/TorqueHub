package torquehub.torquehub.business.interfaces;

import torquehub.torquehub.domain.request.LoginDtos.LoginRequest;
import torquehub.torquehub.domain.request.UserDtos.UserCreateRequest;
import torquehub.torquehub.domain.request.UserDtos.UserUpdateRequest;
import torquehub.torquehub.domain.response.LoginDtos.LoginResponse;
import torquehub.torquehub.domain.response.UserDtos.UserResponse;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse createUser(UserCreateRequest userCreateRequest);

    Optional<UserResponse> getUserById(Long id);

    void deleteUser(Long id);

    boolean userExistsById(Long id);

    boolean userExistsByUsername(String username);

    boolean updateUserById(Long id, UserUpdateRequest userUpdateRequest);

    Optional<UserResponse> findByUsername(String username);

    LoginResponse login(LoginRequest loginRequest);

    Optional<UserResponse> findByEmail(String email);
}
