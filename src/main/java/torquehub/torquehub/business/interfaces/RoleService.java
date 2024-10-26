package torquehub.torquehub.business.interfaces;


import torquehub.torquehub.domain.request.role_dtos.RoleCreateRequest;
import torquehub.torquehub.domain.request.role_dtos.RoleUpdateRequest;
import torquehub.torquehub.domain.response.role_dtos.RoleResponse;

import java.util.List;
import java.util.Optional;


public interface RoleService {

    RoleResponse createRole(RoleCreateRequest roleCreateRequest);

    List<RoleResponse> getAllRoles();

    Optional<RoleResponse> getRoleById(Long id);


    boolean updateRole(long id, RoleUpdateRequest roleUpdateRequest);

    boolean deleteRole(Long id);

}
