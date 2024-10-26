package torquehub.torquehub.business.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.exeption.role_exeptions.RoleDeleteExeption;
import torquehub.torquehub.business.exeption.role_exeptions.RoleUpdateExeption;
import torquehub.torquehub.business.interfaces.RoleService;
import torquehub.torquehub.domain.mapper.RoleMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;
import torquehub.torquehub.domain.request.role_dtos.RoleCreateRequest;
import torquehub.torquehub.domain.request.role_dtos.RoleUpdateRequest;
import torquehub.torquehub.domain.response.role_dtos.RoleResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaRoleRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    private final JpaRoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public RoleServiceImpl(JpaRoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    public RoleResponse createRole(RoleCreateRequest roleCreateRequest) {
        // Check if a role with the same name already exists
        Optional<JpaRole> existingRole = roleRepository.findByName(roleCreateRequest.getRoleName());
        if (existingRole.isPresent()) {
            throw new IllegalArgumentException("Role with name '" + roleCreateRequest.getRoleName() + "' already exists.");
        }

        // If not, create a new role
        JpaRole jpaRole = JpaRole.builder().name(roleCreateRequest.getRoleName()).build();
        JpaRole createdJpaRole = roleRepository.save(jpaRole);
        return roleMapper.toResponse(createdJpaRole);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<RoleResponse> getRoleById(Long id) {
        return roleRepository.findById(id).map(roleMapper::toResponse);
    }

    @Override
    @Transactional
    public boolean updateRole(long id, RoleUpdateRequest roleUpdateRequest) {
        try {
            Optional<JpaRole> roleOptional = roleRepository.findById(id);
            if (roleOptional.isPresent()) {
                JpaRole existingJpaRole = roleOptional.get();
                existingJpaRole.setName(roleUpdateRequest.getName());
                roleRepository.save(existingJpaRole);

                return true;
            } else {
                throw new IllegalArgumentException("Role with ID " + id + " not found.");

            }
        } catch (Exception e) {
            throw new RoleUpdateExeption("Failed to update role: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteRole(Long id) {
        try {
            Optional<JpaRole> roleOptional = roleRepository.findById(id);
            if (roleOptional.isPresent()){
                JpaRole jpaRole = roleOptional.get();
                roleRepository.delete(jpaRole);
                return true;
            } else {
                throw new IllegalArgumentException("Role with ID " + id + " not found.");
            }
        }catch (Exception e){
            throw new RoleDeleteExeption("Failed to delete role: " + e.getMessage(), e);
        }
    }

}
