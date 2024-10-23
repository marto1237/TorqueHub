package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;
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
    public Optional<JpaRole> findByName(String rolename) {
        return roleRepository.findByName(rolename);
    }

    @Override
    public List<JpaRole> findAll() {
        return roleRepository.findAll();
    }


    @Override
    public JpaRole save(JpaRole jpaRole) {
        return roleRepository.save(jpaRole);
    }

    @Override
    public Optional<JpaRole> findById(Long id) {
        return roleRepository.findById(id);
    }

    @Override
    public boolean delete(JpaRole jpaRole) {
        if (roleRepository.existsById(jpaRole.getId())) {
            roleRepository.delete(jpaRole);
            return true;
        } else {
            return false;
        }
    }
}
