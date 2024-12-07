package torquehub.torquehub.persistence.jpa.impl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
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
    @Transactional
    public JpaAnswer save(JpaAnswer jpaAnswer) {
        return answerRepository.save(jpaAnswer);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JpaAnswer> findById(Long id) {
        return answerRepository.findById(id);
    }



    @Override
    public Page<JpaAnswer> findByQuestionId(Long questionId, Pageable pageable) {
        return answerRepository.findByJpaQuestion_Id(questionId, pageable);
    }

    @Override
    public Long countByJpaUserId(Long userId) {
        return answerRepository.countByJpaUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JpaAnswer> findByUserId(Long userId) {
        return answerRepository.findByJpaUser_Id(userId);
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        if (answerRepository.existsById(id)) {
            answerRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}
