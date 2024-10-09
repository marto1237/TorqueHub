package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import torquehub.torquehub.domain.model.Role;
import torquehub.torquehub.domain.request.RoleDtos.RoleCreateRequest;
import torquehub.torquehub.domain.request.RoleDtos.RoleUpdateRequest;
import torquehub.torquehub.domain.response.RoleDtos.RoleResponse;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toEntity(RoleCreateRequest roleCreateRequest);
    Role toEntity(RoleUpdateRequest roleUpdateRequest);
    RoleResponse toResponse(Role role);


}

