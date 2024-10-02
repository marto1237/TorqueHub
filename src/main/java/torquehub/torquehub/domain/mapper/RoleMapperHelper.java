package torquehub.torquehub.domain.mapper;

import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import torquehub.torquehub.domain.model.Role;
import torquehub.torquehub.persistence.repository.RoleRepository;

@Component
public class RoleMapperHelper {

    @Autowired
    private RoleRepository roleRepository;

    @Named("stringToRole")
    public Role stringToRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
    }
}
