package torquehub.torquehub.persistence.repository;
import torquehub.torquehub.domain.model.Answer;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository {
    Answer save(Answer answer);
    Optional<Answer> findById(Long id);
    List<Answer> findByQuestionId(Long questionId);
    List<Answer> findByUserId(Long userId);
    boolean deleteById(Long id);

}
