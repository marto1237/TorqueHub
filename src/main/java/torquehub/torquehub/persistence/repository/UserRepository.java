package torquehub.torquehub.persistence.repository;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository {
    List<User> findAll();
    User findById(long userId);
    User save(User user);
    void deleteById(long userId);
    boolean existsById(long userId);
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    int count();

}
