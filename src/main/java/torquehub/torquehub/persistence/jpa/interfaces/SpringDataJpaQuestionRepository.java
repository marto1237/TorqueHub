package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;

import java.util.List;
import java.util.Optional;

public interface SpringDataJpaQuestionRepository extends JpaRepository<JpaQuestion, Long> {
    Page<JpaQuestion> findAll(Pageable pageable);
    Optional<JpaQuestion> findById(Long aLong);
    Optional<JpaQuestion> findByTitle(String title);
    List<JpaQuestion> findByJpaUserId(Long aLong);

    @Query("SELECT DISTINCT q FROM JpaQuestion q JOIN q.jpaTags t WHERE t.name IN :tags")
    Page<JpaQuestion> findQuestionsByTagNames(@Param("tags") List<String> tags, Pageable pageable);


    @Query("SELECT q FROM JpaQuestion q LEFT JOIN FETCH q.jpaAnswers a LEFT JOIN FETCH a.jpaComments WHERE q.id = :id")
    Optional<JpaQuestion> findByIdWithAnswersAndComments(@Param("id") Long id);

    @Query("SELECT q FROM JpaQuestion q LEFT JOIN FETCH q.jpaAnswers WHERE q.id = :id")
    Optional<JpaQuestion> findByIdWithAnswers(@Param("id") Long id);

    Page<JpaQuestion> findAllByOrderByAskedTimeDesc(Pageable pageable);
    Page<JpaQuestion> findAllByOrderByLastActivityTimeDesc(Pageable pageable);
    Page<JpaQuestion> findAllByOrderByVotesDesc(Pageable pageable);
    Page<JpaQuestion> findAllByOrderByViewsDesc(Pageable pageable);

    @Query("SELECT q FROM JpaQuestion q LEFT JOIN FETCH q.jpaAnswers WHERE SIZE(q.jpaAnswers) = 0")
    Page<JpaQuestion> findQuestionsWithNoAnswers(Pageable pageable);
}
