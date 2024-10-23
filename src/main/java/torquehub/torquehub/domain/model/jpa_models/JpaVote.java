package torquehub.torquehub.domain.model.jpa_models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import torquehub.torquehub.domain.model.plain_models.BaseVote;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "votes")
public class JpaVote extends BaseVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private JpaUser jpaUser;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = true)
    private JpaQuestion jpaQuestion;

    @ManyToOne
    @JoinColumn(name = "answer_id", nullable = true)
    private JpaAnswer jpaAnswer;

    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = true)
    private JpaComment jpaComment;

    private boolean upvote;

    @Column(name = "voted_at")
    private LocalDateTime votedAt;
}
