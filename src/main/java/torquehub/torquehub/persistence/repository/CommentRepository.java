package torquehub.torquehub.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import torquehub.torquehub.domain.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    Comment save(Comment comment);
    Optional<Comment> findById(Long commentId);
    boolean deleteById(Long commentId);
    List<Comment> findByAnswerId(Long answerId);
    List<Comment> findByUserId(Long userId);
    Page<Comment> findByAnswerId(Long answerId, Pageable pageable);

}
