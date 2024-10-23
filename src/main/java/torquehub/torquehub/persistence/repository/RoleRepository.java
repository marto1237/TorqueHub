package torquehub.torquehub.persistence.repository;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository{
    Optional<JpaRole> findByName(String rolename);
    List<JpaRole> findAll();
    JpaRole save(JpaRole jpaRole);
    Optional<JpaRole> findById(Long id);
    boolean delete(JpaRole jpaRole);
}
