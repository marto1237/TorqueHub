package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;
import torquehub.torquehub.domain.request.role_dtos.RoleCreateRequest;
import torquehub.torquehub.domain.request.role_dtos.RoleUpdateRequest;
import torquehub.torquehub.domain.response.role_dtos.RoleResponse;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    JpaRole toEntity(RoleCreateRequest roleCreateRequest);
    JpaRole toEntity(RoleUpdateRequest roleUpdateRequest);
    RoleResponse toResponse(JpaRole jpaRole);


}

