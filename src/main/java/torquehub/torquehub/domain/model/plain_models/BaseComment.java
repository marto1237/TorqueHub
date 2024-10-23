package torquehub.torquehub.domain.model.plain_models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseComment {

    private Long id;
    private JpaUser jpaUser;
    private JpaAnswer jpaAnswer;
    private String text;
    private int votes = 0;
    private LocalDateTime commentedTime;
    private boolean isEdited = false;
}
