package torquehub.torquehub.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import torquehub.torquehub.domain.model.jpa_models.JpaComment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    JpaComment save(JpaComment jpaComment);
    Optional<JpaComment> findById(Long commentId);
    boolean deleteById(Long commentId);
    List<JpaComment> findByAnswerId(Long answerId);
    List<JpaComment> findByUserId(Long userId);
    Page<JpaComment> findByAnswerId(Long answerId, Pageable pageable);

}
