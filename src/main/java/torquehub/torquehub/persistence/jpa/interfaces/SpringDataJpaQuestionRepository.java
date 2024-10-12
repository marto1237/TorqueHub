package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.model.Tag;

import java.util.List;
import java.util.Optional;

public interface SpringDataJpaQuestionRepository extends JpaRepository<Question, Long> {
    Page<Question> findAll(Pageable pageable);
    Optional<Question> findById(Long aLong);
    Optional<Question> findByTitle(String title);
    List<Question> findByUserId(Long aLong);

    @Query("SELECT DISTINCT q FROM Question q JOIN q.tags t WHERE t.name IN :tags")
    Page<Question> findQuestionsByTagNames(@Param("tags") List<String> tags, Pageable pageable);


    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.answers a LEFT JOIN FETCH a.comments WHERE q.id = :id")
    Optional<Question> findByIdWithAnswersAndComments(@Param("id") Long id);

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.answers WHERE q.id = :id")
    Optional<Question> findByIdWithAnswers(@Param("id") Long id);

    Page<Question> findAllByOrderByAskedTimeDesc(Pageable pageable);
    Page<Question> findAllByOrderByLastActivityTimeDesc(Pageable pageable);
    Page<Question> findAllByOrderByVotesDesc(Pageable pageable);
    Page<Question> findAllByOrderByViewsDesc(Pageable pageable);

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.answers WHERE SIZE(q.answers) = 0")
    Page<Question> findQuestionsWithNoAnswers(Pageable pageable);
}
