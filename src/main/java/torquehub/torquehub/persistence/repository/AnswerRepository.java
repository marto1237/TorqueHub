package torquehub.torquehub.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.Answer;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionId(Long questionId);
    List<Answer> findByUserId(Long userId);

}
