package torquehub.torquehub.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import torquehub.torquehub.business.interfaces.RoleService;
import torquehub.torquehub.configuration.SecurityConfig;
import torquehub.torquehub.configuration.jwt.auth.UnauthorizedEntityPoint;
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.jwt.token.AccessTokenEncoder;
import torquehub.torquehub.configuration.jwt.token.impl.AccessTokenEncoderDecoderImpl;
import torquehub.torquehub.configuration.jwt.token.impl.BlacklistService;
import torquehub.torquehub.configuration.utils.TokenUtil;
import torquehub.torquehub.controllers.RoleController;
import torquehub.torquehub.domain.request.role_dtos.RoleCreateRequest;
import torquehub.torquehub.domain.request.role_dtos.RoleUpdateRequest;
import torquehub.torquehub.domain.response.role_dtos.RoleResponse;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
@Import({SecurityConfig.class, AccessTokenEncoderDecoderImpl.class, BlacklistService.class})
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    private static final String VALID_TOKEN = "Bearer valid_token";

    @MockBean
    private AccessTokenEncoder accessTokenEncoder;

    @MockBean
    private AccessTokenDecoder accessTokenDecoder;

    @MockBean
    private TokenUtil tokenUtil;

    @MockBean
    private UnauthorizedEntityPoint unauthorizedEntityPoint;

    @BeforeEach
    void setUp() {
        AccessToken mockAccessToken = mock(AccessToken.class);
        given(mockAccessToken.getUsername()).willReturn("user");
        given(mockAccessToken.getRole()).willReturn("MODERATOR");

        // Return a valid token when a valid token string is provided
        given(accessTokenDecoder.decode("valid_token")).willReturn(mockAccessToken);
        // Ensure tokenUtil also recognizes it as valid
        given(tokenUtil.getUserIdFromToken("Bearer valid_token")).willReturn(2L);
    }


    @Test
    @WithMockUser(authorities = "MODERATOR")
    void getRoles_shouldReturnRolesList() throws Exception {
        RoleResponse roleResponse = new RoleResponse(1L, "MODERATOR");
        Mockito.when(roleService.getAllRoles()).thenReturn(Collections.singletonList(roleResponse));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("MODERATOR"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getRoles_withAdminAuthority_shouldReturnRolesList() throws Exception {
        RoleResponse roleResponse = new RoleResponse(1L, "ADMIN");
        Mockito.when(roleService.getAllRoles()).thenReturn(Collections.singletonList(roleResponse));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("ADMIN"));
    }

    @Test
    @WithMockUser(authorities = "MODERATOR")
    void getRoleById_whenRoleExists_shouldReturnRole() throws Exception {
        RoleResponse roleResponse = new RoleResponse(1L, "ADMIN");
        Mockito.when(roleService.getRoleById(1L)).thenReturn(Optional.of(roleResponse));

        mockMvc.perform(get("/roles/1")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("ADMIN"));
    }

    @Test
    @WithMockUser(authorities = "MODERATOR")
    void getRoleById_whenRoleDoesNotExist_shouldReturnNotFound() throws Exception {
        Mockito.when(roleService.getRoleById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/roles/1")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "MODERATOR")
    void createRole_shouldReturnCreatedRole() throws Exception {
        RoleResponse roleResponse = new RoleResponse(2L, "USER");
        Mockito.when(roleService.createRole(Mockito.any(RoleCreateRequest.class))).thenReturn(roleResponse);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"USER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("USER"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createRole_withAdminAuthority_shouldReturnCreatedRole() throws Exception {
        RoleResponse roleResponse = new RoleResponse(2L, "USER");
        Mockito.when(roleService.createRole(Mockito.any(RoleCreateRequest.class))).thenReturn(roleResponse);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"NEW_USER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("USER"));
    }

    @Test
    @WithMockUser(authorities = "MODERATOR")
    void updateRole_whenRoleExists_shouldReturnSuccessMessage() throws Exception {
        Mockito.when(roleService.updateRole(Mockito.eq(1L), Mockito.any(RoleUpdateRequest.class)))
                .thenReturn(true);

        mockMvc.perform(put("/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"USER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role updated successfully."));
    }

    @Test
    @WithMockUser(authorities = "MODERATOR")
    void updateRole_whenRoleDoesNotExist_shouldReturnNotFoundMessage() throws Exception {
        Mockito.when(roleService.updateRole(Mockito.eq(1L), Mockito.any(RoleUpdateRequest.class)))
                .thenReturn(false);

        mockMvc.perform(put("/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"NEW_ROLE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role with ID 1 not found."));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteRole_whenRoleExists_shouldReturnSuccessMessage() throws Exception {
        Mockito.when(roleService.getRoleById(1L)).thenReturn(Optional.of(new RoleResponse(1L, "USER")));

        Mockito.when(roleService.deleteRole(1L)).thenReturn(true);

        mockMvc.perform(delete("/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role deleted successfully."));
    }


    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteRole_withAdminAuthority_whenRoleExists_shouldReturnSuccessMessage() throws Exception {
        Mockito.when(roleService.getRoleById(1L)).thenReturn(Optional.of(new RoleResponse(1L, "USER")));
        // If deleteRole() returns any object, mock it with the appropriate response (e.g., boolean)
        Mockito.when(roleService.deleteRole(1L)).thenReturn(true); // Assuming it returns true on success

        mockMvc.perform(delete("/roles/1")
                        .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role deleted successfully."));
    }



    @Test
    @WithMockUser(authorities = "MODERATOR")
    void deleteRole_whenRoleDoesNotExist_shouldReturnNotFoundMessage() throws Exception {
        Mockito.when(roleService.getRoleById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/roles/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Role with ID 1 not found."));
    }

    @Test
    void getRoles_whenUserIsUnauthorized_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createRole_whenUserIsUnauthorized_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"USER\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void getRoleById_whenUserIsUnauthorized_shouldReturnForbidden() throws Exception {
        AccessToken mockAccessToken = mock(AccessToken.class);
        given(mockAccessToken.getRole()).willReturn("USER");
        given(mockAccessToken.getUsername()).willReturn("testUser");
        given(accessTokenDecoder.decode("valid_token")).willReturn(mockAccessToken);

        mockMvc.perform(get("/roles/1")
                        .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isForbidden());
    }




    @Test
    void updateRole_whenUserIsUnauthorized_shouldReturnForbidden() throws Exception {
        mockMvc.perform(put("/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"NEW_ROLE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteRole_whenUserIsUnauthorized_shouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/roles/1"))
                .andExpect(status().isForbidden());
    }
}
