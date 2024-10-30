package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.jpa_models.JpaBookmark;

import java.util.List;
import java.util.Optional;

public interface SpringDataJpaBookmarkRepository extends JpaRepository<JpaBookmark, Long> {
    List<JpaBookmark> findByJpaUserId(Long userId);
    Optional<JpaBookmark> findByJpaUserIdAndJpaQuestionId(Long userId, Long questionId);
    Optional<JpaBookmark> findByJpaUserIdAndJpaAnswerId(Long userId, Long answerId);
    Page<JpaBookmark> findByJpaUserIdAndJpaQuestionIsNotNull(Long userId, Pageable pageable);
    Page<JpaBookmark> findByJpaUserIdAndJpaAnswerIsNotNull(Long userId, Pageable pageable);
}
