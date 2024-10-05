package torquehub.torquehub.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.*;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserAndAnswer(User user, Answer answer);
    Optional<Vote> findByUserAndQuestion(User user, Question question);
    Optional<Vote> findByUserAndComment(User user, Comment comment);
}
