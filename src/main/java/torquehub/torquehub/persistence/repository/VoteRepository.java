package torquehub.torquehub.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.*;

import java.util.Optional;

@Repository
public interface VoteRepository {

    Optional<Vote> findByUserAndAnswer(User user, Answer answer);
    boolean delete(Vote vote);
    Vote save(Vote vote);
    Optional<Vote> findByUserAndComment(User user, Comment comment);
    Optional<Vote> findByUserAndQuestion(User user, Question question);

}
