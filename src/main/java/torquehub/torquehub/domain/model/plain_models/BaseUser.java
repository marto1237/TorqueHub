package torquehub.torquehub.domain.model.plain_models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import torquehub.torquehub.domain.model.jpa_models.JpaRole;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseUser {

    private Long id;
    private String username;
    private String email;
    private String password;
    private String salt;
    private JpaRole jpaRole;
    private int points = 0;
}
