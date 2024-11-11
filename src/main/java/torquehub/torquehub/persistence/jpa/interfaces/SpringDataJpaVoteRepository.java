package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.jpa_models.*;

import java.util.Optional;

public interface SpringDataJpaVoteRepository extends JpaRepository<JpaVote, Long> {
    Optional<JpaVote> findByJpaUserAndJpaAnswer(JpaUser jpaUser, JpaAnswer jpaAnswer);
    Optional<JpaVote> findByJpaUserIdAndJpaAnswerId(Long userId, Long answerId);
    Optional<JpaVote> findByJpaUserAndJpaQuestion(JpaUser jpaUser, JpaQuestion jpaQuestion);
    Optional<JpaVote> findByJpaUserAndJpaComment(JpaUser jpaUser, JpaComment jpaComment);
    Optional<JpaVote> findTopByJpaUserAndJpaQuestionOrderByVotedAtDesc(JpaUser jpaUser, JpaQuestion jpaQuestion);
    Optional<JpaVote> findByJpaUserIdAndJpaCommentId(Long userId, Long commentId);

}
