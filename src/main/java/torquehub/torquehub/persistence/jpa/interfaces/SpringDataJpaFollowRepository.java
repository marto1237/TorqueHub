package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.jpa_models.JpaFollow;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;

import java.util.List;
import java.util.Optional;

public interface SpringDataJpaFollowRepository extends JpaRepository<JpaFollow, Long> {
    List<JpaFollow> findByJpaUserId(Long userId);
    boolean existsByJpaUserIdAndJpaQuestionId(Long userId, Long questionId);
    boolean existsByJpaUserIdAndJpaAnswerId(Long userId, Long answerId);
    Optional<JpaFollow> findByJpaUserIdAndJpaQuestionId(Long userId, Long questionId);
    Optional<JpaFollow> findByJpaUserIdAndJpaAnswerId(Long userId, Long answerId);
    Optional<JpaFollow> findByJpaUserAndJpaQuestion(JpaUser jpaUser, JpaQuestion jpaQuestion);
    boolean existsByJpaUserAndJpaQuestion(JpaUser jpaUser, JpaQuestion jpaQuestion);
    Page<JpaFollow> findByJpaUserIdAndJpaQuestionIsNotNull(Long userId, Pageable pageable);
    Page<JpaFollow> findByJpaUserIdAndJpaAnswerIsNotNull(Long userId, Pageable pageable);
    List<JpaFollow> findByJpaQuestion_IdAndIsMutedFalse(Long questionId);
    List<JpaFollow> findByJpaAnswer_IdAndIsMutedFalse(Long answerId);


}
