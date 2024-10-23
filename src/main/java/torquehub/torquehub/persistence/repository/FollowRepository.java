package torquehub.torquehub.persistence.repository;

import torquehub.torquehub.domain.model.jpa_models.JpaFollow;

import java.util.List;
import java.util.Optional;

public interface FollowRepository {
    JpaFollow save(JpaFollow jpaFollow);
    Optional<JpaFollow> findById(Long id);
    List<JpaFollow> findByUserId(Long userId);
    Optional<JpaFollow> findByUserIdAndQuestionId(Long userId, Long questionId);
    Optional<JpaFollow> findByUserIdAndAnswerId(Long userId, Long answerId);
    List<JpaFollow> findByFollowedUserId(Long followedUserId);
    boolean delete(JpaFollow jpaFollow);
    boolean deleteById(Long id);
}
