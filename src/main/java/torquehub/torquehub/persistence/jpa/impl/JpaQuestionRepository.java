package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaQuestionRepository;
import torquehub.torquehub.persistence.repository.QuestionRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JpaQuestionRepository implements QuestionRepository {

    private final SpringDataJpaQuestionRepository questionRepository;

    public JpaQuestionRepository(SpringDataJpaQuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public JpaQuestion save(JpaQuestion jpaQuestion) {
        return questionRepository.save(jpaQuestion);
    }

    @Override
    public Optional<JpaQuestion> findById(Long questionId) {
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
    public Page<JpaQuestion> findAll(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }

    @Override
    public Page<JpaQuestion> findQuestionsByTags(List<JpaTag> jpaTagEntities, Pageable pageable) {
        return questionRepository.findQuestionsByTagNames(jpaTagEntities.stream().map(JpaTag::getName).collect(Collectors.toList()), pageable);
    }

    @Override
    public List<JpaQuestion> findByJpaUserId(Long userId) {
        return questionRepository.findByJpaUserId(userId);
    }

    @Override
    public Page<JpaQuestion> findAllByOrderByAskedTimeDesc(Pageable pageable) {
        return questionRepository.findAllByOrderByAskedTimeDesc(pageable);
    }


    @Override
    public Page<JpaQuestion> findAllByOrderByLastActivityTimeDesc(Pageable pageable) {
        return questionRepository.findAllByOrderByLastActivityTimeDesc(pageable);
    }

    @Override
    public Page<JpaQuestion> findAllByOrderByVotesDesc(Pageable pageable) {
        return questionRepository.findAllByOrderByVotesDesc(pageable);
    }

    @Override
    public Page<JpaQuestion> findAllByOrderByViewCountDesc(Pageable pageable) {
        return questionRepository.findAllByOrderByViewsDesc(pageable);
    }

    @Override
    public Page<JpaQuestion> findQuestionsWithNoAnswers(Pageable pageable) {
        return questionRepository.findQuestionsWithNoAnswers(pageable);
    }
}
