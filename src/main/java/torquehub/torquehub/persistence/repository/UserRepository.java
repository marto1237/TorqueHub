package torquehub.torquehub.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository{
     List<User> findAll();
     User save(User user);
     Optional<User> findById(Long userId);
     boolean delete(User user);
     boolean existsById(Long userId);
     boolean existsByUsername(String username);
     Optional<User> findByUsername(String username);
     Optional<User> findByEmail(String email);
}
