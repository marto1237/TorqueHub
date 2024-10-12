package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.Answer;

import java.util.List;

public interface SpringDataJpaAnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByUserId(Long userId);
    Page<Answer> findByQuestionId(Long questionId, Pageable pageable);
}
