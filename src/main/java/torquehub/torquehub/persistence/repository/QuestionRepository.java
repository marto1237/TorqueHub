package torquehub.torquehub.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.model.Tag;
import torquehub.torquehub.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    Page<Question> findAll(Pageable pageable);
    Optional<Question> findById(Long aLong);
    Optional<Question> findByTitle(String title);
    List<Question> findByUserId(Long aLong);

    @Query("SELECT q FROM Question q JOIN q.tags t WHERE t.name IN :tags GROUP BY q")
    Page<Question> findQuestionsByTags(@Param("tags") List<Tag> tags, Pageable pageable);




}
