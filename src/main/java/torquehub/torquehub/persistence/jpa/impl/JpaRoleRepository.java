package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.Role;
import torquehub.torquehub.domain.response.RoleDtos.RoleResponse;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaRoleRepository;
import torquehub.torquehub.persistence.repository.RoleRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaRoleRepository implements RoleRepository {

    private final SpringDataJpaRoleRepository roleRepository;

    public JpaRoleRepository(SpringDataJpaRoleRepository springDataJpaRoleRepository) {
        this.roleRepository = springDataJpaRoleRepository;
    }

    @Override
    public Optional<Role> findByName(String rolename) {
        return roleRepository.findByName(rolename);
    }

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }


    @Override
    public Role save(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    @Override
    public boolean delete(Role role) {
        if (roleRepository.existsById(role.getId())) {
            roleRepository.delete(role);
            return true;
        } else {
            return false;
        }
    }
}
