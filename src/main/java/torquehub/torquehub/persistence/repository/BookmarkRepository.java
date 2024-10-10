package torquehub.torquehub.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.Bookmark;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository {
    Bookmark save(Bookmark bookmark);
    Optional<Bookmark> findById(Long id);
    List<Bookmark> findByUserId(Long userId);
    Optional<Bookmark> findByUserIdAndQuestionId(Long userId, Long questionId);
    Optional<Bookmark> findByUserIdAndAnswerId(Long userId, Long answerId);
    boolean delete(Bookmark bookmark);
    void deleteById(Long id);
}
