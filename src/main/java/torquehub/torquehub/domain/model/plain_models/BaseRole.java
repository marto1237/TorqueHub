package torquehub.torquehub.domain.model.plain_models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseRole {

    private Long id;
    private String name;
}