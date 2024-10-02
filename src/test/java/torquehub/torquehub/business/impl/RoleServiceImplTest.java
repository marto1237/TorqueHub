package torquehub.torquehub.business.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import torquehub.torquehub.domain.mapper.RoleMapper;
import torquehub.torquehub.domain.model.Role;
import torquehub.torquehub.domain.request.RoleDtos.RoleCreateRequest;
import torquehub.torquehub.domain.request.RoleDtos.RoleUpdateRequest;
import torquehub.torquehub.domain.response.RoleDtos.RoleResponse;
import torquehub.torquehub.persistence.repository.RoleRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleServiceImplTest {

    @InjectMocks
    private RoleServiceImpl roleService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    private Role testRole;
    private RoleCreateRequest roleCreateRequest;
    private RoleUpdateRequest roleUpdateRequest;
    private RoleResponse roleResponse;

    @BeforeEach
    void setUp() {
        testRole = Role.builder().id(1L).name("ADMIN").build();

        roleCreateRequest = RoleCreateRequest.builder()
                .roleName("ADMIN")
                .build();

        roleUpdateRequest = RoleUpdateRequest.builder()
                .name("ADMIN")
                .build();

        roleResponse = RoleResponse.builder()
                .id(1L)
                .name("ADMIN")
                .build();
    }

    @Test
    void shouldCreateRoleSuccessfully() {
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);
        when(roleMapper.toResponse(any(Role.class))).thenReturn(roleResponse);

        RoleResponse response = roleService.createRole(roleCreateRequest);

        assertNotNull(response);
        assertEquals("ADMIN", response.getName());
        assertEquals(1L, response.getId());


        verify(roleRepository, times(1)).findByName("ADMIN");
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void shouldThrowException_whenRoleAlreadyExists() {
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(testRole));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roleService.createRole(roleCreateRequest);
        });
        assertEquals("Role with name 'ADMIN' already exists.", exception.getMessage());

        verify(roleRepository, times(1)).findByName("ADMIN");
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldGetAllRolesSuccessfully(){
        when(roleRepository.findAll()).thenReturn(Arrays.asList(testRole));
        when(roleMapper.toResponse(testRole)).thenReturn(roleResponse);

        List<RoleResponse> response = roleService.getAllRoles();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("ADMIN", response.get(0).getName());
        assertEquals(1L, response.get(0).getId());

        verify(roleRepository, times(1)).findAll();
        verify(roleMapper, times(1)).toResponse(testRole);
    }

    @Test
    void shouldGetRoleByIdSuccessfully(){
        when(roleRepository.findById(testRole.getId())).thenReturn(Optional.of(testRole));
        when(roleMapper.toResponse(testRole)).thenReturn(roleResponse);

        Optional<RoleResponse> response = roleService.getRoleById(1L);

        assertTrue(response.isPresent());
        assertEquals("ADMIN", response.get().getName());
        assertEquals(1L, response.get().getId());

        verify(roleRepository, times(1)).findById(1L);
        verify(roleMapper, times(1)).toResponse(testRole);
    }

    @Test
    void shouldReturnEmpty_whenRoleDoesNotExist(){
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<RoleResponse> response = roleService.getRoleById(1L);

        assertTrue(response.isEmpty());

        verify(roleRepository, times(1)).findById(1L);
        verify(roleMapper, never()).toResponse(any(Role.class));
    }

    @Test
    void shouldUpdateRoleSuccessfully(){
        when(roleRepository.findById(testRole.getId())).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        boolean response = roleService.updateRole(1L, roleUpdateRequest);

        assertTrue(response);

        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void shouldThrowException_whenRoleDoesNotExist(){
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());


        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.updateRole(1L, roleUpdateRequest);
        });

        assertEquals("Failed to update role: Role with ID 1 not found.", exception.getMessage());

        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldDeleteRoleSuccessfully(){
        roleService.deleteRole(1L);

        verify(roleRepository, times(1)).deleteById(1L);
    }
}
