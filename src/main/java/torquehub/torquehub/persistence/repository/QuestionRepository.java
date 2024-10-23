package torquehub.torquehub.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository  {
    JpaQuestion save(JpaQuestion jpaQuestion);
    Optional<JpaQuestion> findById(Long questionId);
    boolean deleteById(Long questionId);
    Page<JpaQuestion> findAll(Pageable pageable);
    Page<JpaQuestion> findQuestionsByTags(List<JpaTag> jpaTagEntities, Pageable pageable);
    List<JpaQuestion> findByJpaUserId(Long userId);
    Page<JpaQuestion> findAllByOrderByAskedTimeDesc(Pageable pageable);
    Page<JpaQuestion> findAllByOrderByLastActivityTimeDesc(Pageable pageable);
    Page<JpaQuestion> findAllByOrderByVotesDesc(Pageable pageable);
    Page<JpaQuestion> findAllByOrderByViewCountDesc(Pageable pageable);
    Page<JpaQuestion> findQuestionsWithNoAnswers(Pageable pageable);






}
