package torquehub.torquehub.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAnswerId(Long answerId);
    List<Comment> findByUserId(Long userId);
}
