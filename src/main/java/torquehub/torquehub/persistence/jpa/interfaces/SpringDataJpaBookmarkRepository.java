package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.Bookmark;

import java.util.List;
import java.util.Optional;

public interface SpringDataJpaBookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByUserId(Long userId);
    Optional<Bookmark> findByUserIdAndQuestionId(Long userId, Long questionId);
    Optional<Bookmark> findByUserIdAndAnswerId(Long userId, Long answerId);
}
