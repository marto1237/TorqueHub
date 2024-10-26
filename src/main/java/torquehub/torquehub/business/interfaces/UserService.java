package torquehub.torquehub.business.interfaces;

import torquehub.torquehub.domain.request.login_dtos.LoginRequest;
import torquehub.torquehub.domain.request.user_dtos.UserCreateRequest;
import torquehub.torquehub.domain.request.user_dtos.UserUpdateRequest;
import torquehub.torquehub.domain.request.user_promotion_dtos.UserPromotionRequest;
import torquehub.torquehub.domain.response.login_dtos.LoginResponse;
import torquehub.torquehub.domain.response.user_dtos.UserResponse;
import torquehub.torquehub.domain.response.user_promotion_dtos.UserPromotionResponse;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse createUser(UserCreateRequest userCreateRequest);

    Optional<UserResponse> getUserById(Long id);

    boolean deleteUser(Long id);

    boolean userExistsById(Long id);

    boolean userExistsByUsername(String username);

    boolean updateUserById(Long id, UserUpdateRequest userUpdateRequest);

    UserPromotionResponse promoteUser(UserPromotionRequest request);

    Optional<UserResponse> findByUsername(String username);

    LoginResponse login(LoginRequest loginRequest);

    Optional<UserResponse> findByEmail(String email);
}
