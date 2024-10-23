package torquehub.torquehub.domain.model.plain_models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.model.jpa_models.JpaComment;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseVote {
    private Long id;
    private JpaUser jpaUser;
    private JpaQuestion jpaQuestion;
    private JpaAnswer jpaAnswer;
    private JpaComment jpaComment;
    private boolean upvote;
    private LocalDateTime votedAt;
}
