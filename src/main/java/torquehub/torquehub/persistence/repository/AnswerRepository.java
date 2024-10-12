package torquehub.torquehub.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import torquehub.torquehub.domain.model.Answer;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository {
    Answer save(Answer answer);

    Optional<Answer> findById(Long id);

    List<Answer> findByUserId(Long userId);

    boolean deleteById(Long id);

    Page<Answer> findByQuestionId(Long questionId, Pageable pageable);

}
