package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.Follow;
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
    public Follow save(Follow follow) {
        return followRepository.save(follow);
    }

    @Override
    public Optional<Follow> findById(Long id) {
        return followRepository.findById(id);
    }

    @Override
    public List<Follow> findByUserId(Long userId) {
        return followRepository.findByUserId(userId);
    }

    @Override
    public Optional<Follow> findByUserIdAndQuestionId(Long userId, Long questionId) {
        return followRepository.findByUserIdAndQuestionId(userId, questionId);
    }

    @Override
    public Optional<Follow> findByUserIdAndAnswerId(Long userId, Long answerId) {
        return followRepository.findByUserIdAndAnswerId(userId, answerId);
    }

    @Override
    public List<Follow> findByFollowedUserId(Long followedUserId) {
        return followRepository.findByUserId(followedUserId);
    }

    @Override
    public boolean delete(Follow follow) {
        if (followRepository.existsById(follow.getId())) {
            followRepository.delete(follow);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Optional<Follow> findByUserIdAndFollowedUserId(Long userId, Long followedUserId) {
        return followRepository.findByUserIdAndQuestionId(userId, followedUserId);
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
