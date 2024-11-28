package torquehub.torquehub.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.UserService;

@RestController
@RequestMapping("/users/validation")
public class UserValidationController {
    private final UserService userService;

    public UserValidationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Boolean> validateUser(@PathVariable Long userId) {
        boolean exists = userService.userExistsById(userId);
        return ResponseEntity.ok(exists);
    }
}
