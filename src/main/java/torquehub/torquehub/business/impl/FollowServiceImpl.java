package torquehub.torquehub.business.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.exeption.filter_exeption.FilterFollowAnswerExeption;
import torquehub.torquehub.business.exeption.filter_exeption.FilterFollowQuestionExeption;
import torquehub.torquehub.business.interfaces.FollowService;
import torquehub.torquehub.domain.mapper.FollowMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.model.jpa_models.JpaFollow;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.request.follow_dtos.FollowAnswerRequest;
import torquehub.torquehub.domain.request.follow_dtos.FollowQuestionRequest;
import torquehub.torquehub.domain.request.follow_dtos.FollowedAnswerRequest;
import torquehub.torquehub.domain.request.follow_dtos.FollowedQuestionRequest;
import torquehub.torquehub.domain.response.follow_dtos.FollowResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaAnswerRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaFollowRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaQuestionRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaUserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FollowServiceImpl implements FollowService {

    private final JpaFollowRepository followRepository;
    private final JpaUserRepository userRepository;
    private final JpaQuestionRepository questionRepository;
    private final JpaAnswerRepository answerRepository;
    private final FollowMapper followMapper;

    public FollowServiceImpl(JpaFollowRepository followRepository,
                             JpaUserRepository userRepository,
                             JpaQuestionRepository questionRepository,
                             JpaAnswerRepository answerRepository,
                             FollowMapper followMapper) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.followMapper = followMapper;
    }


    @Override
    @CacheEvict(value = {"followedQuestions", "followedAnswers"}, key = "#followQuestionRequest.userId", allEntries = true)
    @Transactional
    public FollowResponse toggleFollowQuestion(FollowQuestionRequest followQuestionRequest) {
        try {
            JpaFollow jpaFollow = followRepository.findByUserIdAndQuestionId(followQuestionRequest.getUserId(), followQuestionRequest.getQuestionId())
                    .orElse(null);
            if (jpaFollow != null) {
                followRepository.delete(jpaFollow);
                return null;
            }else {
                JpaUser jpaUser = userRepository.findById(followQuestionRequest.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                JpaQuestion jpaQuestion = questionRepository.findById(followQuestionRequest.getQuestionId())
                        .orElseThrow(() -> new RuntimeException("Question not found"));

                JpaFollow newJpaFollow = JpaFollow.builder()
                        .jpaUser(jpaUser)
                        .jpaQuestion(jpaQuestion)
                        .followedAt(LocalDateTime.now())
                        .build();

                JpaFollow savedJpaFollow = followRepository.save(newJpaFollow);
                return followMapper.toResponse(savedJpaFollow);
            }


        }catch (Exception e) {
            throw new FilterFollowQuestionExeption("Error following question", e);
        }
    }

    @Override
    @CacheEvict(value = "questionDetailsByIdAndUser", key = "#followAnswerRequest.answerId")
    @Transactional
    public FollowResponse toggleFollowAnswer(FollowAnswerRequest followAnswerRequest) {
        try {
            JpaFollow jpaFollow = followRepository.findByUserIdAndAnswerId(followAnswerRequest.getUserId(), followAnswerRequest.getAnswerId())
                    .orElse(null);
            if (jpaFollow != null) {
                followRepository.delete(jpaFollow);
                return null;
            }else {
                JpaUser jpaUser = userRepository.findById(followAnswerRequest.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                JpaAnswer jpaAnswer = answerRepository.findById(followAnswerRequest.getAnswerId())
                        .orElseThrow(() -> new RuntimeException("Answer not found"));

                JpaFollow newJpaFollow = JpaFollow.builder()
                        .jpaUser(jpaUser)
                        .jpaAnswer(jpaAnswer)
                        .followedAt(LocalDateTime.now())
                        .build();

                JpaFollow savedJpaFollow = followRepository.save(newJpaFollow);
                return followMapper.toResponse(savedJpaFollow);
            }
        }catch (Exception e) {
            throw new FilterFollowAnswerExeption("Error following answer",e);
        }
    }

    @Transactional
    public boolean muteNotifications(Long followId) {
        JpaFollow jpaFollow = followRepository.findById(followId)
                .orElseThrow(() -> new IllegalArgumentException("Follow not found"));
        jpaFollow.setMuted(true);
        followRepository.save(jpaFollow);
        return true;
    }

    public List<FollowResponse> getUserFollows(Long userId) {
        List<JpaFollow> jpaFollows = followRepository.findByUserId(userId);
        if (jpaFollows.isEmpty()) {
            return List.of();
        }else {
            return jpaFollows.stream()
                    .map(followMapper::toResponse)
                    .toList();
        }
    }

    @Override
    @Cacheable(value = "followedQuestions", key = "#request.userId + '-' + #request.pageable.pageNumber")
    public Page<FollowResponse> getFollowedQuestions(FollowedQuestionRequest request) {
        Page<JpaFollow> followedQuestions = followRepository.findByUserIdAndJpaQuestionIsNotNull(request.getUserId(), request.getPageable());
        return followedQuestions.map(followMapper::toResponse);
    }

    @Override
    @Cacheable(value = "followedAnswers", key = "#request.userId + '-' + #request.pageable.pageNumber")
    public Page<FollowResponse> getFollowedAnswers(FollowedAnswerRequest request) {
        Page<JpaFollow> followedAnswers = followRepository.findByUserIdAndJpaAnswerIsNotNull(request.getUserId(), request.getPageable());
        return followedAnswers.map(followMapper::toResponse);
    }

    @Transactional
    @Override
    public boolean batchMuteFollows(List<Long> followIds) {
        List<JpaFollow> follows = followRepository.findAllById(followIds);
        for (JpaFollow follow : follows) {
            follow.setMuted(true);
        }
        followRepository.saveAll(follows);
        return true;
    }

    @Transactional
    @Override
    public boolean batchUnfollow(List<Long> followIds) {
        List<JpaFollow> follows = followRepository.findAllById(followIds);
        followRepository.deleteAll(follows);
        return true;
    }
}
