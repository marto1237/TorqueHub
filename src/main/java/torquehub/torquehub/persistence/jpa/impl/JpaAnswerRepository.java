package torquehub.torquehub.persistence.jpa.impl;
import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.Answer;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaAnswerRepository;
import torquehub.torquehub.persistence.repository.AnswerRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaAnswerRepository implements AnswerRepository {

    private final SpringDataJpaAnswerRepository answerRepository;

    public JpaAnswerRepository(SpringDataJpaAnswerRepository answerRepository) {
        this.answerRepository = answerRepository;
    }
    @Override
    public Answer save(Answer answer) {
        return answerRepository.save(answer);
    }

    @Override
    public Optional<Answer> findById(Long id) {
        return answerRepository.findById(id);
    }

    @Override
    public List<Answer> findByQuestionId(Long questionId) {
        return answerRepository.findByQuestionId(questionId);
    }

    @Override
    public List<Answer> findByUserId(Long userId) {
        return answerRepository.findByUserId(userId);
    }

    @Override
    public void deleteById(Long id) {
        answerRepository.deleteById(id);
    }
}
