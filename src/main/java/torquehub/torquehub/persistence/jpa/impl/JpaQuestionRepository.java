package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.model.Tag;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaQuestionRepository;
import torquehub.torquehub.persistence.repository.QuestionRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaQuestionRepository implements QuestionRepository {

    private final SpringDataJpaQuestionRepository questionRepository;

    public JpaQuestionRepository(SpringDataJpaQuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public Question save(Question question) {
        return questionRepository.save(question);
    }

    @Override
    public Optional<Question> findById(Long questionId) {
        return questionRepository.findById(questionId);
    }

    @Override
    public boolean deleteById(Long questionId) {
        if (questionRepository.existsById(questionId)) {
            questionRepository.deleteById(questionId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Page<Question> findAll(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }

    @Override
    public Page<Question> findQuestionsByTags(List<Tag> tagEntities, Pageable pageable) {
        return questionRepository.findQuestionsByTags(tagEntities, pageable);
    }

    @Override
    public List<Question> findByUserId(Long userId) {
        return questionRepository.findByUserId(userId);
    }
}
