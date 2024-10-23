package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.jpa_models.JpaComment;

import java.util.List;

public interface SpringDataJpaCommentRepository extends JpaRepository<JpaComment, Long> {
    List<JpaComment> findByJpaAnswer_Id(Long answerId);
    List<JpaComment> findByJpaUser_Id(Long userId);
    Page<JpaComment> findByJpaAnswer_Id(Long answerId, Pageable pageable);
    List<JpaComment> findAllByJpaAnswer_Id(Long answerId);
}
