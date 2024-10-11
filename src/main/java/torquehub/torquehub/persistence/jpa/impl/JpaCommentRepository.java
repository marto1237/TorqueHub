package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.Comment;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaCommentRepository;
import torquehub.torquehub.persistence.repository.CommentRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaCommentRepository implements CommentRepository {
    private final SpringDataJpaCommentRepository commentRepository;

    public JpaCommentRepository(SpringDataJpaCommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    @Override
    public List<Comment> findByAnswerId(Long answerId) {
        return commentRepository.findByAnswerId(answerId);
    }

    @Override
    public List<Comment> findByUserId(Long userId) {
        return commentRepository.findByUserId(userId);
    }

    @Override
    public Page<Comment> findByAnswerId(Long answerId, Pageable pageable) {
        return commentRepository.findByAnswerId(answerId, pageable);
    }

    @Override
    public boolean deleteById(Long id) {
        if (commentRepository.existsById(id)) {
            commentRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}
