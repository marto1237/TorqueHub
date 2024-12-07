package torquehub.torquehub.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository {
    JpaAnswer save(JpaAnswer jpaAnswer);

    Optional<JpaAnswer> findById(Long id);

    List<JpaAnswer> findByUserId(Long userId);

    boolean deleteById(Long id);

    Page<JpaAnswer> findByQuestionId(Long questionId, Pageable pageable);

    Long countByJpaUserId(Long userId);

}
