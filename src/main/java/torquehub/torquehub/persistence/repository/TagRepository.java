package torquehub.torquehub.persistence.repository;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;

import java.util.List;
import java.util.Optional;


@Repository
public interface TagRepository {

    JpaTag save(JpaTag jpaTag);
    Optional<JpaTag> findById(Long tagId);
    Optional<JpaTag> findByName(String tagName);
    boolean delete(JpaTag jpaTag);
    boolean existsById(Long tagId);
    List<JpaTag> findAll();
}
