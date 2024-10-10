package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.Role;

import java.util.Optional;

public interface SpringDataJpaRoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
