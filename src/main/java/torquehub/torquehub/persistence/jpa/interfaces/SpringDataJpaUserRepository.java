package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;

import java.util.Optional;

public interface SpringDataJpaUserRepository extends JpaRepository<JpaUser, Long> {
    Optional<JpaUser> findByUsername(String username);
    Optional<JpaUser> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
