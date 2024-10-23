package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;

import java.util.Optional;

public interface SpringDataJpaRoleRepository extends JpaRepository<JpaRole, Long> {
    Optional<JpaRole> findByName(String name);
}
