package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.jpa_models.JpaComment;
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
    public JpaComment save(JpaComment jpaComment) {
        return commentRepository.save(jpaComment);
    }

    @Override
    public Optional<JpaComment> findById(Long id) {
        return commentRepository.findById(id);
    }

    @Override
    public List<JpaComment> findByAnswerId(Long answerId) {
        return commentRepository.findByJpaAnswer_Id(answerId);
    }

    @Override
    public List<JpaComment> findByUserId(Long userId) {
        return commentRepository.findByJpaUser_Id(userId);
    }

    @Override
    public Page<JpaComment> findByAnswerId(Long answerId, Pageable pageable) {
        return commentRepository.findByJpaAnswer_Id(answerId, pageable);
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
