package torquehub.torquehub.domain.mapper;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;
import torquehub.torquehub.persistence.repository.RoleRepository;

@Component
public class RoleMapperHelper {

    private final RoleRepository roleRepository;

    public RoleMapperHelper(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Named("stringToRole")
    public JpaRole stringToRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
    }
}
