package torquehub.torquehub.persistence.jpa.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.Follow;

import java.util.List;
import java.util.Optional;

public interface SpringDataJpaFollowRepository extends JpaRepository<Follow, Long> {
    List<Follow> findByUserId(Long userId);
    boolean existsByUserIdAndQuestionId(Long userId, Long questionId);
    Optional<Follow> findByUserIdAndQuestionId(Long userId, Long questionId);
    Optional<Follow> findByUserIdAndAnswerId(Long userId, Long answerId);
}
