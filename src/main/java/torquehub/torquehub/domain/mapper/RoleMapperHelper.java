package torquehub.torquehub.domain.mapper;

import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;
import torquehub.torquehub.persistence.repository.RoleRepository;

@Component
public class RoleMapperHelper {

    @Autowired
    private RoleRepository roleRepository;

    @Named("stringToRole")
    public JpaRole stringToRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
    }
}
