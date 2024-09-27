package torquehub.torquehub.domain.request.LoginDtos;

import jakarta.validation.constraints.Email;
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
public class LoginRequest {

    @NotBlank
    @Email(message = "Email should be valid")
    String email;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password;
}
