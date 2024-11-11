package torquehub.torquehub.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import torquehub.torquehub.domain.mapper.RoleMapperHelper;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;
import torquehub.torquehub.persistence.repository.RoleRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleMapperHelperTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleMapperHelper roleMapperHelper;

    private JpaRole jpaRole;

    @BeforeEach
    void setUp() {
        jpaRole = new JpaRole();
        jpaRole.setName("USER_ROLE");
    }

    @Test
    void testStringToRole_WhenRoleExists() {
        when(roleRepository.findByName("USER_ROLE")).thenReturn(Optional.of(jpaRole));

        JpaRole result = roleMapperHelper.stringToRole("USER_ROLE");

        assertNotNull(result);
        assertEquals("USER_ROLE", result.getName());
        verify(roleRepository).findByName("USER_ROLE");
    }

    @Test
    void testStringToRole_WhenRoleDoesNotExist() {
        when(roleRepository.findByName("NON_EXISTENT_ROLE")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                roleMapperHelper.stringToRole("NON_EXISTENT_ROLE")
        );

        assertEquals("Role not found: NON_EXISTENT_ROLE", exception.getMessage());
        verify(roleRepository).findByName("NON_EXISTENT_ROLE");
    }
}

