package torquehub.torquehub.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.controllers.DTO.UserDto;
import torquehub.torquehub.domain.User;
import torquehub.torquehub.business.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("{id}")
    public ResponseEntity<User> getStudent(@PathVariable(value = "id") final long id){
        final Optional<User> userOptional = Optional.ofNullable(userService.getUserById(id));
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(userOptional.get());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserDto> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        UserDto userDto = new UserDto();
        userDto.setId(createdUser.getId());
        userDto.setUsername(createdUser.getUsername());
        userDto.setEmail(createdUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<Void> updateUser(@PathVariable("id") long id,
                                          @RequestBody User user) {

        user.setId(id);
        userService.updateUserById(user);

        return ResponseEntity.noContent().build();
    }
}
