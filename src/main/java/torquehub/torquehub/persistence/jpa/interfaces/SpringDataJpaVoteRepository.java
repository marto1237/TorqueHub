package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.*;

import java.util.Optional;

public interface SpringDataJpaVoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserAndAnswer(User user, Answer answer);
    Optional<Vote> findByUserAndQuestion(User user, Question question);
    Optional<Vote> findByUserAndComment(User user, Comment comment);

}
