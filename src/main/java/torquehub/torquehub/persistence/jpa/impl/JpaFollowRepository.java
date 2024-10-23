package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.jpa_models.JpaFollow;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaFollowRepository;
import torquehub.torquehub.persistence.repository.FollowRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaFollowRepository implements FollowRepository {
    private final SpringDataJpaFollowRepository followRepository;

    public JpaFollowRepository(SpringDataJpaFollowRepository followRepository) {
        this.followRepository = followRepository;
    }

    @Override
    public JpaFollow save(JpaFollow jpaFollow) {
        return followRepository.save(jpaFollow);
    }

    @Override
    public Optional<JpaFollow> findById(Long id) {
        return followRepository.findById(id);
    }

    @Override
    public List<JpaFollow> findByUserId(Long userId) {
        return followRepository.findByJpaUserId(userId);
    }

    @Override
    public Optional<JpaFollow> findByUserIdAndQuestionId(Long userId, Long questionId) {
        return followRepository.findByJpaUserIdAndJpaQuestionId(userId, questionId);
    }

    @Override
    public Optional<JpaFollow> findByUserIdAndAnswerId(Long userId, Long answerId) {
        return followRepository.findByJpaUserIdAndJpaAnswerId(userId, answerId);
    }

    @Override
    public List<JpaFollow> findByFollowedUserId(Long followedUserId) {
        return followRepository.findByJpaUserId(followedUserId);
    }

    @Override
    public boolean delete(JpaFollow jpaFollow) {
        if (followRepository.existsById(jpaFollow.getId())) {
            followRepository.delete(jpaFollow);
            return true;
        } else {
            return false;
        }
    }




    @Override
    public boolean deleteById(Long id) {
        if (followRepository.existsById(id)) {
            followRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

}
