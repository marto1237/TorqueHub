package torquehub.torquehub.domain.request.role_dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleCreateRequest {

    @NotBlank
    @Size(min = 3, max = 50,message = "Role must be between 3 and 50 characters")
    private String roleName;
}
