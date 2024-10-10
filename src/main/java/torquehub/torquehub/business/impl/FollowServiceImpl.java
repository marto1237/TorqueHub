package torquehub.torquehub.business.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.interfaces.FollowService;
import torquehub.torquehub.domain.mapper.FollowMapper;
import torquehub.torquehub.domain.model.Answer;
import torquehub.torquehub.domain.model.Follow;
import torquehub.torquehub.domain.model.Question;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.request.FollowDtos.FollowAnswerRequest;
import torquehub.torquehub.domain.request.FollowDtos.FollowQuestionRequest;
import torquehub.torquehub.domain.response.FollowRequest.FollowResponse;
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
    @Transactional
    public FollowResponse followQuestion(FollowQuestionRequest followQuestionRequest) {
        try {
            Follow follow = followRepository.findByUserIdAndQuestionId(followQuestionRequest.getUserId(), followQuestionRequest.getQuestionId())
                    .orElse(null);
            if (follow != null) {
                followRepository.delete(follow);
                return null;
            }else {
                User user = userRepository.findById(followQuestionRequest.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                Question question = questionRepository.findById(followQuestionRequest.getQuestionId())
                        .orElseThrow(() -> new RuntimeException("Question not found"));

                Follow newFollow = Follow.builder()
                        .user(user)
                        .question(question)
                        .followedAt(LocalDateTime.now())
                        .build();

                Follow savedFollow = followRepository.save(newFollow);
                return followMapper.toResponse(savedFollow);
            }


        }catch (Exception e) {
            throw new RuntimeException("Error following question");
        }
    }

    public FollowResponse followResponse(FollowAnswerRequest followAnswerRequest) {
        try {
            Follow follow = followRepository.findByUserIdAndAnswerId(followAnswerRequest.getUserId(), followAnswerRequest.getAnswerId())
                    .orElse(null);
            if (follow != null) {
                followRepository.delete(follow);
                return null;
            }else {
                User user = userRepository.findById(followAnswerRequest.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                Answer answer = answerRepository.findById(followAnswerRequest.getAnswerId())
                        .orElseThrow(() -> new RuntimeException("Answer not found"));

                Follow newFollow = Follow.builder()
                        .user(user)
                        .answer(answer)
                        .followedAt(LocalDateTime.now())
                        .build();

                Follow savedFollow = followRepository.save(newFollow);
                return followMapper.toResponse(savedFollow);
            }
        }catch (Exception e) {
            throw new RuntimeException("Error following answer");
        }
    }

    public boolean muteNotifications(Long followId) {
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new IllegalArgumentException("Follow not found"));
        follow.setMuted(true);
        followRepository.save(follow);
        return true;
    }

    public List<FollowResponse> getUserFollows(Long userId) {
        List<Follow> follows = followRepository.findByUserId(userId);
        if (follows.isEmpty()) {
            return null;
        }else {
            return follows.stream()
                    .map(followMapper::toResponse)
                    .toList();
        }
    }
}
