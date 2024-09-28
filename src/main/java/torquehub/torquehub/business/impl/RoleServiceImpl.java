package torquehub.torquehub.business.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import torquehub.torquehub.business.interfaces.RoleService;
import torquehub.torquehub.domain.model.Role;
import torquehub.torquehub.domain.request.RoleDtos.RoleCreateRequest;
import torquehub.torquehub.domain.request.RoleDtos.RoleUpdateRequest;
import torquehub.torquehub.domain.response.RoleDtos.RoleResponse;
import torquehub.torquehub.persistence.repository.RoleRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

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
        return mapToResponse(createdRole);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RoleResponse> getRoleById(Long id) {
        return roleRepository.findById(id).map(this::mapToResponse);
    }

    @Override
    public boolean updateRole(long id, RoleUpdateRequest roleUpdateRequest) {
        Optional<Role> roleOptional = roleRepository.findById(id);
        if (roleOptional.isPresent()) {
            Role existingRole = roleOptional.get();
            existingRole.setName(roleUpdateRequest.getName());
            Role updatedRole = roleRepository.save(existingRole);

            return true;
        }
        return false;
    }

    @Override
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }

    // Helper method to map Role entity to RoleResponse DTO
    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }
}
