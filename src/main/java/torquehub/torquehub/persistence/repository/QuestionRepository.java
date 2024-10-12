package torquehub.torquehub.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.model.Tag;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository  {
    Question save(Question question);
    Optional<Question> findById(Long questionId);
    boolean deleteById(Long questionId);
    Page<Question> findAll(Pageable pageable);
    Page<Question> findQuestionsByTags(List<Tag> tagEntities, Pageable pageable);
    List<Question> findByUserId(Long userId);
    Page<Question> findAllByOrderByAskedTimeDesc(Pageable pageable);
    Page<Question> findAllByOrderByLastActivityTimeDesc(Pageable pageable);
    Page<Question> findAllByOrderByVotesDesc(Pageable pageable);
    Page<Question> findAllByOrderByViewCountDesc(Pageable pageable);
    Page<Question> findQuestionsWithNoAnswers(Pageable pageable);






}
