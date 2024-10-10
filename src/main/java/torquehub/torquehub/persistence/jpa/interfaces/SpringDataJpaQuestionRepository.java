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

    @Query("SELECT q FROM Question q JOIN q.tags t WHERE t.name IN :tags GROUP BY q")
    Page<Question> findQuestionsByTags(@Param("tags") List<Tag> tags, Pageable pageable);

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.answers a LEFT JOIN FETCH a.comments WHERE q.id = :id")
    Optional<Question> findByIdWithAnswersAndComments(@Param("id") Long id);

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.answers WHERE q.id = :id")
    Optional<Question> findByIdWithAnswers(@Param("id") Long id);
}
