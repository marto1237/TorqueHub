    package torquehub.torquehub.controllers;

    import jakarta.validation.Valid;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.validation.annotation.Validated;
    import org.springframework.web.bind.annotation.*;
    import torquehub.torquehub.business.interfaces.UserService;
    import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
    import torquehub.torquehub.configuration.utils.TokenUtil;
    import torquehub.torquehub.domain.request.user_dtos.UserCreateRequest;
    import torquehub.torquehub.domain.request.user_dtos.UserUpdateRequest;
    import torquehub.torquehub.domain.request.user_promotion_dtos.UserPromotionRequest;
    import torquehub.torquehub.domain.response.MessageResponse;
    import torquehub.torquehub.domain.response.user_dtos.UserResponse;
    import torquehub.torquehub.domain.response.user_promotion_dtos.UserPromotionResponse;

    import java.util.List;
    import java.util.Optional;

    @RestController
    @RequestMapping("/users")
    @Validated
    public class UserController {

        private final UserService userService;
        private final TokenUtil tokenUtil;

        @Value("${rabbitmq.exchange:default.exchange}")
        private String exchange;

        public UserController(UserService userService, TokenUtil tokenUtil) {
            this.userService = userService;
            this.tokenUtil = tokenUtil;
        }


        @GetMapping
        public List<UserResponse> getUsers() {
            return userService.getAllUsers();
        }

        @GetMapping("/{id}")
        public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
            Optional<UserResponse> user = userService.getUserById(id);
            return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        }

        @PostMapping
        public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest userDto) {
            UserResponse createdUser = userService.createUser(userDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        }

        @PutMapping("/{id}")
        public ResponseEntity<MessageResponse> updateUser(@PathVariable Long id,@Valid @RequestBody UserUpdateRequest updateDto) {
            MessageResponse response = new MessageResponse();
            if (userService.updateUserById(id, updateDto)) {
                response.setMessage("User updated successfully.");
            } else {
                response.setMessage("User with ID " + id + " not found.");
            }
            return  ResponseEntity.ok(response);
        }

        @PutMapping("/{id}/promote")
        @PreAuthorize("hasAuthority('MODERATOR') or hasAuthority('ADMIN')")
        public ResponseEntity<UserPromotionResponse> promoteUser(
                @PathVariable Long id,
                @Valid @RequestBody UserPromotionRequest promotionRequest,
                @RequestHeader("Authorization") String token) {
            try {
                Long promoterId = tokenUtil.getUserIdFromToken(token);
                promotionRequest.setPromoterUserId(promoterId);
                promotionRequest.setPromotedUserId(id);

                UserPromotionResponse response = userService.promoteUser(promotionRequest);
                return ResponseEntity.ok(response);
            } catch (InvalidAccessTokenException e) {
                UserPromotionResponse errorResponse = new UserPromotionResponse();
                errorResponse.setMessage("Invalid token.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains("Invalid role")) {
                    UserPromotionResponse errorResponse = new UserPromotionResponse();
                    errorResponse.setMessage("Invalid role");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }
                UserPromotionResponse errorResponse = new UserPromotionResponse();
                errorResponse.setMessage(e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            } catch (Exception e) {
                UserPromotionResponse errorResponse = new UserPromotionResponse();
                errorResponse.setMessage("An unexpected error occurred.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }


        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('MODERATOR') or hasAuthority('ADMIN')")
        public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
            MessageResponse response = new MessageResponse();
            Optional<UserResponse> user = userService.getUserById(id);
            if (user.isPresent()) {
                userService.deleteUser(id);
                response.setMessage("User deleted successfully.");
                return ResponseEntity.ok(response);
            } else {
                response.setMessage("User with ID " + id + " not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        }

    }
