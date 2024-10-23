package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;

import java.util.Optional;

public interface SpringDataJpaTagRepository extends JpaRepository<JpaTag, Long> {
    Optional<JpaTag> findByName(String name);
}
