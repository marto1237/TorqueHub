package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;

import java.util.List;

public interface SpringDataJpaAnswerRepository extends JpaRepository<JpaAnswer, Long> {
    List<JpaAnswer> findByJpaUser_Id(Long userId);
    Page<JpaAnswer> findByJpaQuestion_Id(Long questionId, Pageable pageable);
    Long countByJpaUserId(Long userId);
}
