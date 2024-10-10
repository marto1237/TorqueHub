package torquehub.torquehub.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import torquehub.torquehub.domain.model.Follow;

import java.util.List;
import java.util.Optional;

public interface FollowRepository {
    Follow save(Follow follow);
    Optional<Follow> findById(Long id);
    List<Follow> findByUserId(Long userId);
    Optional<Follow> findByUserIdAndFollowedUserId(Long userId, Long followedUserId);
    Optional<Follow> findByUserIdAndQuestionId(Long userId, Long questionId);
    Optional<Follow> findByUserIdAndAnswerId(Long userId, Long answerId);
    List<Follow> findByFollowedUserId(Long followedUserId);
    boolean delete(Follow follow);
    boolean deleteById(Long id);
}
