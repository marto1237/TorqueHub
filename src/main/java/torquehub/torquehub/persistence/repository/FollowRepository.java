package torquehub.torquehub.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import torquehub.torquehub.domain.model.jpa_models.JpaFollow;

import java.util.List;
import java.util.Optional;

public interface FollowRepository {
    JpaFollow save(JpaFollow jpaFollow);
    List<JpaFollow> saveAll(List<JpaFollow> jpaFollows);
    Optional<JpaFollow> findById(Long id);
    List<JpaFollow> findAllById(List<Long> followIds);
    List<JpaFollow> findByUserId(Long userId);
    Optional<JpaFollow> findByUserIdAndQuestionId(Long userId, Long questionId);
    Optional<JpaFollow> findByUserIdAndAnswerId(Long userId, Long answerId);
    List<JpaFollow> findByFollowedUserId(Long followedUserId);
    boolean delete(JpaFollow jpaFollow);
    boolean deleteById(Long id);
    boolean deleteAll(List<JpaFollow> jpaFollows);
    Page<JpaFollow> findByUserIdAndJpaQuestionIsNotNull(Long userId, Pageable pageable);
    Page<JpaFollow> findByUserIdAndJpaAnswerIsNotNull(Long userId, Pageable pageable);
}
