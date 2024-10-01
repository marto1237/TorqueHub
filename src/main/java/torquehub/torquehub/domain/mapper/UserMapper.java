package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import torquehub.torquehub.domain.model.Role;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.request.UserDtos.UserCreateRequest;
import torquehub.torquehub.domain.request.UserDtos.UserUpdateRequest;
import torquehub.torquehub.domain.response.UserDtos.UserResponse;
import torquehub.torquehub.persistence.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    private RoleRepository roleRepository;

    // Map UserCreateRequest to User entity
    @Mapping(target = "password", ignore = true) // If password is handled separately
    @Mapping(source = "role", target = "role", qualifiedByName = "stringToRole")
    public abstract User toEntity(UserCreateRequest userCreateRequest);

    // Map UserUpdateRequest to User entity
    @Mapping(source = "email", target = "role", qualifiedByName = "stringToRole")
    public abstract User toEntity(UserUpdateRequest userUpdateRequest);

    // Map User entity to UserResponse DTO
    @Mapping(source = "role.name", target = "role")
    public abstract UserResponse toResponse(User user);

    // Custom method to map String to Role entity
    @Named("stringToRole")
    protected Role stringToRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
    }
}
