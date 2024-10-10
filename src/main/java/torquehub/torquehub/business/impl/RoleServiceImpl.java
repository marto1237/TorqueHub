package torquehub.torquehub.business.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.interfaces.RoleService;
import torquehub.torquehub.domain.mapper.RoleMapper;
import torquehub.torquehub.domain.model.Role;
import torquehub.torquehub.domain.request.RoleDtos.RoleCreateRequest;
import torquehub.torquehub.domain.request.RoleDtos.RoleUpdateRequest;
import torquehub.torquehub.domain.response.RoleDtos.RoleResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaRoleRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Optional<Role> existingRole = roleRepository.findByName(roleCreateRequest.getRoleName());
        if (existingRole.isPresent()) {
            throw new IllegalArgumentException("Role with name '" + roleCreateRequest.getRoleName() + "' already exists.");
        }

        // If not, create a new role
        Role role = Role.builder().name(roleCreateRequest.getRoleName()).build();
        Role createdRole = roleRepository.save(role);
        return roleMapper.toResponse(createdRole);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RoleResponse> getRoleById(Long id) {
        return roleRepository.findById(id).map(roleMapper::toResponse);
    }

    @Override
    @Transactional
    public boolean updateRole(long id, RoleUpdateRequest roleUpdateRequest) {
        try {
            Optional<Role> roleOptional = roleRepository.findById(id);
            if (roleOptional.isPresent()) {
                Role existingRole = roleOptional.get();
                existingRole.setName(roleUpdateRequest.getName());
                roleRepository.save(existingRole);

                return true;
            } else {
                throw new IllegalArgumentException("Role with ID " + id + " not found.");

            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update role: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteRole(Long id) {
        try {
            Optional<Role> roleOptional = roleRepository.findById(id);
            if (roleOptional.isPresent()){
                Role role = roleOptional.get();
                roleRepository.delete(role);
                return true;
            } else {
                throw new IllegalArgumentException("Role with ID " + id + " not found.");
            }
        }catch (Exception e){
            throw new RuntimeException("Failed to delete role: " + e.getMessage());
        }
    }

}
