package torquehub.torquehub.business.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.exeption.question_exeptions.QuestionCreationException;
import torquehub.torquehub.business.interfaces.QuestionService;
import torquehub.torquehub.business.interfaces.ReputationService;
import torquehub.torquehub.business.interfaces.VoteService;
import torquehub.torquehub.domain.ReputationConstants;
import torquehub.torquehub.domain.mapper.AnswerMapper;
import torquehub.torquehub.domain.mapper.CommentMapper;
import torquehub.torquehub.domain.mapper.QuestionMapper;
import torquehub.torquehub.domain.mapper.QuestionMapperContext;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.model.jpa_models.JpaVote;
import torquehub.torquehub.domain.request.question_dtos.QuestionCreateRequest;
import torquehub.torquehub.domain.request.question_dtos.QuestionUpdateRequest;
import torquehub.torquehub.domain.request.reputation_dtos.ReputationUpdateRequest;
import torquehub.torquehub.domain.response.question_dtos.QuestionDetailResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionSummaryResponse;
import torquehub.torquehub.domain.response.reputation_dtos.ReputationResponse;
import torquehub.torquehub.persistence.jpa.impl.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final JpaQuestionRepository questionRepository;
    private final JpaTagRepository tagRepository;
    private final JpaUserRepository userRepository;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final ReputationService reputationService;
    private final JpaVoteRepository voteRepository;
    private final CommentMapper commentMapper;
    private final VoteService voteService;
    private final JpaFollowRepository followRepository;
    private final JpaBookmarkRepository bookmarkRepository;

    public QuestionServiceImpl(JpaQuestionRepository questionRepository,
                               JpaTagRepository tagRepository,
                               JpaUserRepository userRepository,
                               QuestionMapper questionMapper,
                               AnswerMapper answerMapper,
                               ReputationService reputationService,
                               JpaVoteRepository voteRepository,
                               CommentMapper commentMapper,
                               VoteService voteService,
                               JpaFollowRepository followRepository,
                               JpaBookmarkRepository bookmarkRepository) {
        this.questionRepository = questionRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.questionMapper = questionMapper;
        this.reputationService = reputationService;
        this.voteRepository = voteRepository;
        this.commentMapper = commentMapper;
        this.voteService = voteService;
        this.followRepository = followRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.answerMapper = answerMapper;
    }

    private static final String QUESTION_ID_PREFIX = "Question with ID ";
    private static final String NOT_FOUND_SUFFIX = " not found";


    @Override
    public QuestionResponse askQuestion(QuestionCreateRequest questionCreateRequest) {
        try {
            JpaUser jpaUser = userRepository.findById(questionCreateRequest.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User with ID " + questionCreateRequest.getUserId() + NOT_FOUND_SUFFIX));



            Set<JpaTag> jpaTags = convertTagNamesToTags(questionCreateRequest.getTags());

            JpaQuestion jpaQuestion = JpaQuestion.builder()
                    .title(questionCreateRequest.getTitle())
                    .description(questionCreateRequest.getDescription())
                    .jpaTags(jpaTags)
                    .jpaUser(jpaUser)
                    .askedTime(LocalDateTime.now())
                    .bestAnswerId(null)
                    .views(0)
                    .votes(0)
                    .totalAnswers(0)
                    .totalComments(0)
                    .lastActivityTime(LocalDateTime.now())
                    .build();

            JpaQuestion savedJpaQuestion = questionRepository.save(jpaQuestion);

            ReputationUpdateRequest reputationUpdateRequest = new ReputationUpdateRequest(jpaUser.getId(), ReputationConstants.POINTS_NEW_QUESTION);
            ReputationResponse reputationResponse = reputationService.updateReputationForNewQuestion(reputationUpdateRequest);

            QuestionResponse questionResponse = questionMapper.toResponse(savedJpaQuestion);
            questionResponse.setReputationUpdate(reputationResponse);

            return questionResponse;

        } catch (Exception e) {
            throw new QuestionCreationException("Error creating question: " + e.getMessage(), e);
        }


    }

    @Override
    @CacheEvict(value = "questions", key = "#questionId")
    @Transactional
    public boolean deleteQuestion(Long questionId) {
        try {
            Optional<JpaQuestion> optionalQuestion = questionRepository.findById(questionId);
            if (optionalQuestion.isPresent()) {
                JpaQuestion jpaQuestionToDelete = optionalQuestion.get();

                JpaUser jpaUser = jpaQuestionToDelete.getJpaUser();
                ReputationUpdateRequest reputationUpdateRequest = new ReputationUpdateRequest(jpaUser.getId(), ReputationConstants.POINTS_QUESTION_WHEN_DELETED);
                boolean isDeleted = reputationService.updateReputationForQuestionWhenQuestionIsDeleted(reputationUpdateRequest);
                if(!isDeleted) {
                    throw new IllegalArgumentException("Error updating reputation for user with ID " + jpaUser.getId());
                }
                questionRepository.deleteById(questionId);

                return true;
            } else {
                throw new QuestionCreationException(QUESTION_ID_PREFIX + questionId + NOT_FOUND_SUFFIX);

            }
        } catch (Exception e) {
            throw new QuestionCreationException("Failed to delete question: " + e.getMessage(), e);
        }
    }

    @Override
    @CachePut(value = "questions", key = "#questionId")
    @Transactional
    public boolean updateQuestion(Long questionId, QuestionUpdateRequest questionUpdateRequest) {
        try{
            Optional<JpaQuestion> optionalQuestion = questionRepository.findById(questionId);
            if (optionalQuestion.isPresent()) {
                JpaQuestion existingJpaQuestion = optionalQuestion.get();

                // Convert Set<String> to Set<Tag>
                Set<JpaTag> jpaTags = convertTagNamesToTags(questionUpdateRequest.getTags());

                existingJpaQuestion.setTitle(questionUpdateRequest.getTitle());
                existingJpaQuestion.setDescription(questionUpdateRequest.getDescription());
                existingJpaQuestion.setJpaTags(jpaTags);

                questionRepository.save(existingJpaQuestion);
                existingJpaQuestion.setLastActivityTime(LocalDateTime.now());
                return true;
            } else {
                throw new IllegalArgumentException(QUESTION_ID_PREFIX + questionId + NOT_FOUND_SUFFIX);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error updating question with ID " + questionId);
        }

    }


    @Override
    @Cacheable(value = "questionDetailsByIdAndUser", key = "#questionId + '-' + #userId")
    public Optional<QuestionDetailResponse> getQuestionbyId(Long questionId, Pageable pageable) {
        return questionRepository.findById(questionId)
                .map(question -> {
                    QuestionMapperContext context = new QuestionMapperContext(commentMapper, answerMapper, bookmarkRepository, followRepository, voteRepository, null, pageable);
                    return questionMapper.toDetailResponse(question, context);
                });
    }

    @Override
    @Cacheable(value = "questionDetailsByIdAndUser")
    @Transactional
    public Optional<QuestionDetailResponse> getQuestionbyId(Long questionId, Pageable pageable, Long userId) {
        return questionRepository.findById(questionId)
                .map(question -> {
                    QuestionMapperContext context = new QuestionMapperContext(commentMapper, answerMapper, bookmarkRepository, followRepository, voteRepository, userId, pageable);
                    QuestionDetailResponse questionDetailResponse = questionMapper.toDetailResponse(question, context);

                    if (userId != null) {
                        // Retrieve the user's vote status on the question
                        Optional<JpaVote> userVoteOptional = voteRepository.findByUserAndJpaQuestion(userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found")), question);
                        String userVote = userVoteOptional.map(vote -> vote.isUpvote() ? "up" : "down").orElse(null);
                        questionDetailResponse.setUserVote(userVote);

                        // Check follow and bookmark status
                        boolean isFollowing = followRepository.findByUserIdAndQuestionId(userId, questionId).isPresent();
                        boolean isBookmarked = bookmarkRepository.findByUserIdAndJpaQuestionId(userId, questionId).isPresent();
                        questionDetailResponse.setIsFollowing(isFollowing);
                        questionDetailResponse.setIsBookmarked(isBookmarked);
                    }
                    return questionDetailResponse;
                });
    }

    @Override
    @Cacheable(value = "allQuestionsList", key = "#pageable.pageNumber")
    public Page<QuestionSummaryResponse> getAllQuestions(Pageable pageable) {
        Page<JpaQuestion> questionsPage = questionRepository.findAll(pageable);
        return questionsPage.map(questionMapper::toSummaryResponse);
    }

    @Override
    @Cacheable(value = "userQuestions", key = "#userId")
    public Optional<List<QuestionSummaryResponse>> getQuestionsByUser(Long userId) {
        List<JpaQuestion> jpaQuestions = questionRepository.findByJpaUserId(userId);

        if (jpaQuestions.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(jpaQuestions.stream()
                    .map(questionMapper::toSummaryResponse)
                    .toList());
        }
    }

    @Override
    @Transactional
    public ReputationResponse upvoteQuestion(Long questionId, Long userId) {
        JpaQuestion question = findQuestionById(questionId);
        JpaUser user = findUserById(userId);
        return voteService.handleUpvote(user, question);
    }

    @Override
    @Transactional
    public ReputationResponse downvoteQuestion(Long questionId, Long userId) {
        JpaQuestion question = findQuestionById(questionId);
        JpaUser user = findUserById(userId);
        return voteService.handleDownvote(user, question);
    }

    @Override
    public boolean incrementQuestionView(Long questionId) {
        try {
            JpaQuestion jpaQuestion = questionRepository.findById(questionId)
                    .orElseThrow(() -> new IllegalArgumentException(QUESTION_ID_PREFIX + questionId + NOT_FOUND_SUFFIX));
            jpaQuestion.setViews(jpaQuestion.getViews() + 1);
            questionRepository.save(jpaQuestion);
            return true;
        } catch (Exception e) {
            throw new QuestionCreationException("Error incrementing question view count: " + e.getMessage(),e);
        }
    }

    @Override
    public Long getQuestionCountOfUser(Long userId) {
        return questionRepository.countByJpaUserId(userId);
    }

    private Set<JpaTag> convertTagNamesToTags(Set<String> tagNames) {
        return tagNames.stream()
                .map(tagName -> {
                    JpaTag tag = tagRepository.findByName(tagName)
                            .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagName));

                    tag.setUsageCount(tag.getUsageCount() + 1);
                    tagRepository.save(tag);

                    return tag;
                })
                .collect(Collectors.toSet());
    }


    private JpaQuestion findQuestionById(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException(QUESTION_ID_PREFIX + questionId + NOT_FOUND_SUFFIX));
    }

    private JpaUser findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + NOT_FOUND_SUFFIX));
    }

}

