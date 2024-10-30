package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public List<JpaFollow> saveAll(List<JpaFollow> jpaFollows) {
        return followRepository.saveAll(jpaFollows);
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
    @Transactional
    public boolean deleteById(Long id) {
        if (followRepository.existsById(id)) {
            followRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteAll(List<JpaFollow> jpaFollows) {
        if (jpaFollows.isEmpty()) {
            return false;
        }
        List<Long> followIds = jpaFollows.stream()
                .map(JpaFollow::getId)
                .toList();

        followRepository.deleteAll(jpaFollows);

        return followIds.stream().noneMatch(followRepository::existsById);
    }

    @Override
    public Page<JpaFollow> findByUserIdAndJpaQuestionIsNotNull(Long userId, Pageable pageable) {
        return followRepository.findByJpaUserIdAndJpaQuestionIsNotNull(userId, pageable);
    }

    @Override
    public Page<JpaFollow> findByUserIdAndJpaAnswerIsNotNull(Long userId, Pageable pageable) {
        return followRepository.findByJpaUserIdAndJpaAnswerIsNotNull(userId, pageable);
    }

    @Override
    public List<JpaFollow> findAllById(List<Long> followIds){
        return followRepository.findAllById(followIds);
    }


}
