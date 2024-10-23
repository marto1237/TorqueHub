package torquehub.torquehub.persistence.repository;

import torquehub.torquehub.domain.model.jpa_models.JpaBookmark;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository {
    JpaBookmark save(JpaBookmark jpaBookmark);
    Optional<JpaBookmark> findById(Long id);
    List<JpaBookmark> findByUserId(Long userId);
    Optional<JpaBookmark> findByUserIdAndJpaQuestionId(Long userId, Long questionId);
    Optional<JpaBookmark> findByUserIdAndJpaAnswerId(Long userId, Long answerId);
    boolean delete(JpaBookmark jpaBookmark);
    void deleteById(Long id);
}
