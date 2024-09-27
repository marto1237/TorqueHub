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

    Optional<UserResponse> getUserById(long id);

    void deleteUser(long id);

    boolean userExistsById(long id);

    boolean userExistsByUsername(String username);

    boolean updateUserById(long id, UserUpdateRequest userUpdateRequest);

    Optional<UserResponse> findByUsername(String username);

    LoginResponse login(LoginRequest loginRequest);

    Optional<UserResponse> findByEmail(String email);
}
