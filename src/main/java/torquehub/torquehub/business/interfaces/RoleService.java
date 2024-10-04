package torquehub.torquehub.business.interfaces;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import torquehub.torquehub.domain.model.Role;
import torquehub.torquehub.domain.request.RoleDtos.RoleCreateRequest;
import torquehub.torquehub.domain.request.RoleDtos.RoleUpdateRequest;
import torquehub.torquehub.domain.response.RoleDtos.RoleResponse;
import torquehub.torquehub.domain.response.UserDtos.UserResponse;
import torquehub.torquehub.persistence.repository.RoleRepository;

import java.util.List;
import java.util.Optional;


public interface RoleService {

    RoleResponse createRole(RoleCreateRequest roleCreateRequest);

    List<RoleResponse> getAllRoles();

    Optional<RoleResponse> getRoleById(Long id);


    boolean updateRole(long id, RoleUpdateRequest roleUpdateRequest);

    boolean deleteRole(Long id);

}
