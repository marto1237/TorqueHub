package torquehub.torquehub.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    Optional<Question> findById(Long aLong);
    Optional<Question> findByTitle(String title);
    Optional<List<Question>> findByUserId(Long aLong);
    Optional<List<Question>> findByTags(String tag);


}
