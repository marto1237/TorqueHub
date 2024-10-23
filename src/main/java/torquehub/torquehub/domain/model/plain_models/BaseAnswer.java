package torquehub.torquehub.domain.model.plain_models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaComment;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.model.jpa_models.JpaVote;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseAnswer {
    private  Long id;
    private String text;
    private JpaQuestion jpaQuestion;
    private List<JpaComment> jpaComments = new ArrayList<>();
    private JpaUser jpaUser;
    private int votes = 0;
    private Set<JpaVote> votesList = new HashSet<>();
    private LocalDateTime answeredTime;
    private boolean isEdited = false;
}
