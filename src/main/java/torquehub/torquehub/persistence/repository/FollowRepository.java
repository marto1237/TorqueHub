package torquehub.torquehub.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.Bookmark;
import torquehub.torquehub.domain.model.Follow;
import torquehub.torquehub.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    List<Follow> findByUserId(Long userId);
    boolean existsByUserIdAndQuestionId(Long userId, Long questionId);
    Optional<Follow> findByUserIdAndQuestionId(Long userId, Long questionId);
    Optional<Follow> findByUserIdAndAnswerId(Long userId, Long answerId);
}
