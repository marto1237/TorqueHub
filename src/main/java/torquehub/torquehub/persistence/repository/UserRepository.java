package torquehub.torquehub.persistence.repository;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository{
     List<JpaUser> findAll();
     JpaUser save(JpaUser jpaUser);
     Optional<JpaUser> findById(Long userId);
     boolean delete(JpaUser jpaUser);
     boolean existsById(Long userId);
     boolean existsByUsername(String username);
     Optional<JpaUser> findByUsername(String username);
     Optional<JpaUser> findByEmail(String email);
}
