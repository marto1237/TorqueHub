package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import torquehub.torquehub.domain.model.Role;
import torquehub.torquehub.domain.request.RoleDtos.RoleCreateRequest;
import torquehub.torquehub.domain.request.RoleDtos.RoleUpdateRequest;
import torquehub.torquehub.domain.response.RoleDtos.RoleResponse;
import torquehub.torquehub.persistence.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toEntity(RoleCreateRequest roleCreateRequest);
    Role toEntity(RoleUpdateRequest roleUpdateRequest);
    RoleResponse toResponse(Role role);


}

