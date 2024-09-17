package torquehub.torquehub.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import torquehub.torquehub.domain.User;
import torquehub.torquehub.persistence.repository.UserRepository;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }
    public User createUser(User user) {
        return userRepository.save(user);
    }
    public User getUserById(long id) {
        return userRepository.findById(id);
    }

    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    public boolean userExistsById(long id) {
        return userRepository.existsById(id);
    }

    public boolean userExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public void updateUserById(User user) {
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findById(user.getId()));
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("USER_ID_INVALID");
        }

        User updateUser = optionalUser.get();
        updateUser.setUsername(user.getUsername());
        updateUser.setEmail(user.getEmail());
        updateUser.setPassword(user.getPassword());

        userRepository.save(updateUser);
    }
}
