package torquehub.torquehub.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.Role;
import torquehub.torquehub.domain.response.RoleDtos.RoleResponse;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface RoleRepository{
    Optional<Role> findByName(String rolename);
    List<Role> findAll();
    Role save(Role role);
    Optional<Role> findById(Long id);
    boolean delete(Role role);
}
