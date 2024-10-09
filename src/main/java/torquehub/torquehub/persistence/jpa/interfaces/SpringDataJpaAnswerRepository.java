package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.Answer;

import java.util.List;

public interface SpringDataJpaAnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionId(Long questionId);
    List<Answer> findByUserId(Long userId);
}
