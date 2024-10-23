package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;
import torquehub.torquehub.domain.request.RoleDtos.RoleCreateRequest;
import torquehub.torquehub.domain.request.RoleDtos.RoleUpdateRequest;
import torquehub.torquehub.domain.response.RoleDtos.RoleResponse;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    JpaRole toEntity(RoleCreateRequest roleCreateRequest);
    JpaRole toEntity(RoleUpdateRequest roleUpdateRequest);
    RoleResponse toResponse(JpaRole jpaRole);


}

