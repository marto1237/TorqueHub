package torquehub.torquehub.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
public class User {
    private Long id;

    private String username;

    private String email;

    private String password;
}
