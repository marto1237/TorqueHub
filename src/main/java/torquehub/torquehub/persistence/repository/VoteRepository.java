package torquehub.torquehub.persistence.repository;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.jpa_models.*;

import java.util.Optional;

@Repository
public interface VoteRepository {

    Optional<JpaVote> findByUserAndJpaAnswer(JpaUser jpaUser, JpaAnswer jpaAnswer);
    boolean delete(JpaVote jpaVote);
    JpaVote save(JpaVote jpaVote);
    Optional<JpaVote> findTopByJpaUserAndJpaQuestionOrderByVotedAtDesc(JpaUser jpaUser, JpaQuestion jpaQuestion);
    Optional<JpaVote> findByUserAndJpaComment(JpaUser jpaUser, JpaComment jpaComment);
    Optional<JpaVote> findByUserAndJpaQuestion(JpaUser jpaUser, JpaQuestion jpaQuestion);

}
