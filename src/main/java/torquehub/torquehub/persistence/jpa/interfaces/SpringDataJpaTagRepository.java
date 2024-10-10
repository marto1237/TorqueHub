package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.Tag;

import java.util.Optional;

public interface SpringDataJpaTagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}
