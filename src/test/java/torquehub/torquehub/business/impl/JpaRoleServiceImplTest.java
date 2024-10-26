package torquehub.torquehub.business.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import torquehub.torquehub.domain.mapper.RoleMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;
import torquehub.torquehub.domain.request.role_dtos.RoleCreateRequest;
import torquehub.torquehub.domain.request.role_dtos.RoleUpdateRequest;
import torquehub.torquehub.domain.response.role_dtos.RoleResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaRoleRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaRoleServiceImplTest {

    @InjectMocks
    private RoleServiceImpl roleService;

    @Mock
    private JpaRoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    private JpaRole testJpaRole;
    private final Long roleId = 1L;
    private RoleCreateRequest roleCreateRequest;
    private RoleUpdateRequest roleUpdateRequest;
    private RoleResponse roleResponse;

    @BeforeEach
    void setUp() {
        testJpaRole = JpaRole.builder().id(roleId).name("ADMIN").build();

        roleCreateRequest = RoleCreateRequest.builder()
                .roleName("ADMIN")
                .build();

        roleUpdateRequest = RoleUpdateRequest.builder()
                .name("ADMIN")
                .build();

        roleResponse = RoleResponse.builder()
                .id(roleId)
                .name("ADMIN")
                .build();
    }

    @Test
    void shouldCreateRoleSuccessfully() {
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(JpaRole.class))).thenReturn(testJpaRole);
        when(roleMapper.toResponse(any(JpaRole.class))).thenReturn(roleResponse);

        RoleResponse response = roleService.createRole(roleCreateRequest);

        assertNotNull(response);
        assertEquals("ADMIN", response.getName());
        assertEquals(roleId, response.getId());


        verify(roleRepository, times(1)).findByName("ADMIN");
        verify(roleRepository, times(1)).save(any(JpaRole.class));
    }

    @Test
    void shouldThrowException_whenRoleAlreadyExists() {
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(testJpaRole));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roleService.createRole(roleCreateRequest);
        });
        assertEquals("Role with name 'ADMIN' already exists.", exception.getMessage());

        verify(roleRepository, times(1)).findByName("ADMIN");
        verify(roleRepository, never()).save(any(JpaRole.class));
    }

    @Test
    void shouldGetAllRolesSuccessfully(){
        when(roleRepository.findAll()).thenReturn(Arrays.asList(testJpaRole));
        when(roleMapper.toResponse(testJpaRole)).thenReturn(roleResponse);

        List<RoleResponse> response = roleService.getAllRoles();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("ADMIN", response.get(0).getName());
        assertEquals(roleId, response.get(0).getId());

        verify(roleRepository, times(1)).findAll();
        verify(roleMapper, times(1)).toResponse(testJpaRole);
    }

    @Test
    void shouldGetRoleByIdSuccessfully(){
        when(roleRepository.findById(testJpaRole.getId())).thenReturn(Optional.of(testJpaRole));
        when(roleMapper.toResponse(testJpaRole)).thenReturn(roleResponse);

        Optional<RoleResponse> response = roleService.getRoleById(1L);

        assertTrue(response.isPresent());
        assertEquals("ADMIN", response.get().getName());
        assertEquals(roleId, response.get().getId());

        verify(roleRepository, times(1)).findById(1L);
        verify(roleMapper, times(1)).toResponse(testJpaRole);
    }

    @Test
    void shouldReturnEmpty_whenRoleDoesNotExist(){
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        Optional<RoleResponse> response = roleService.getRoleById(1L);

        assertTrue(response.isEmpty());

        verify(roleRepository, times(1)).findById(roleId);
        verify(roleMapper, never()).toResponse(any(JpaRole.class));
    }

    @Test
    void shouldUpdateRoleSuccessfully(){
        when(roleRepository.findById(testJpaRole.getId())).thenReturn(Optional.of(testJpaRole));
        when(roleRepository.save(any(JpaRole.class))).thenReturn(testJpaRole);

        boolean response = roleService.updateRole(roleId, roleUpdateRequest);

        assertTrue(response);

        verify(roleRepository, times(1)).findById(roleId);
        verify(roleRepository, times(1)).save(any(JpaRole.class));
    }

    @Test
    void shouldThrowException_whenRoleDoesNotExist(){
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());


        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.updateRole(roleId, roleUpdateRequest);
        });

        assertEquals("Failed to update role: Role with ID 1 not found.", exception.getMessage());

        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, never()).save(any(JpaRole.class));
    }

    @Test
    void shouldDeleteRoleSuccessfully() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(new JpaRole()));

        boolean result = roleService.deleteRole(roleId);

        assertTrue(result);
        verify(roleRepository, times(1)).delete(any(JpaRole.class));
    }

    @Test
    void shouldThrowExceptionWhenRoleToDeleteDoesNotExist() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.deleteRole(roleId);
        });

        assertEquals("Failed to delete role: Role with ID " + roleId + " not found.", exception.getMessage());
        verify(roleRepository, times(1)).findById(roleId);
        verify(roleRepository, never()).delete(any(JpaRole.class));
    }
}
