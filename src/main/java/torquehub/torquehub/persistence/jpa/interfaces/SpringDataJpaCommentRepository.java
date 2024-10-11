package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.Comment;

import java.util.List;

public interface SpringDataJpaCommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAnswerId(Long answerId);
    List<Comment> findByUserId(Long userId);
    Page<Comment> findByAnswerId(Long answerId, Pageable pageable);
    List<Comment> findAllByAnswerId(Long answerId);
}
