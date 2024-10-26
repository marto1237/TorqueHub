package torquehub.torquehub.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.business.interfaces.RoleService;
import torquehub.torquehub.domain.request.role_dtos.RoleCreateRequest;
import torquehub.torquehub.domain.request.role_dtos.RoleUpdateRequest;
import torquehub.torquehub.domain.response.MessageResponse;
import torquehub.torquehub.domain.response.role_dtos.RoleResponse;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/roles")
@Validated
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<RoleResponse> getRoles() {
        return roleService.getAllRoles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
        Optional<RoleResponse> role = roleService.getRoleById(id);
        return role.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleCreateRequest roleDto) {
        RoleResponse createdRole = roleService.createRole(roleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest roleDto) {
        MessageResponse response = new MessageResponse();
        if (roleService.updateRole(id, roleDto)) {
            response.setMessage("Role updated successfully.");
        }else {
            response.setMessage("Role with ID " + id + " not found.");
        }
        return  ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse>deleteRole(@PathVariable Long id) {
        MessageResponse response = new MessageResponse();
        Optional<RoleResponse> role = roleService.getRoleById(id);
        if (role.isPresent()) {
            roleService.deleteRole(id);
            response.setMessage("Role deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.setMessage("Role with ID " + id + " not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
