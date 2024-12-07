package torquehub.torquehub.business.impl;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.exeption.BookmarkAlreadyExistsException;
import torquehub.torquehub.business.interfaces.BookmarkService;
import torquehub.torquehub.domain.mapper.AnswerMapper;
import torquehub.torquehub.domain.mapper.BookmarkMapper;
import torquehub.torquehub.domain.mapper.CommentMapper;
import torquehub.torquehub.domain.mapper.QuestionMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.model.jpa_models.JpaBookmark;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkAnswerRequest;
import torquehub.torquehub.domain.request.bookmark_dtos.BookmarkQuestionRequest;
import torquehub.torquehub.domain.response.answer_dtos.AnswerResponse;
import torquehub.torquehub.domain.response.bookmark_dtos.BookmarkResponse;
import torquehub.torquehub.domain.response.question_dtos.QuestionResponse;
import torquehub.torquehub.persistence.jpa.impl.*;

import java.time.LocalDateTime;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    private final JpaBookmarkRepository bookmarkRepository;
    private final JpaAnswerRepository answerRepository;
    private final JpaUserRepository userRepository;
    private final JpaQuestionRepository questionRepository;
    private final BookmarkMapper bookmarkMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final JpaFollowRepository followRepository;
    private final JpaVoteRepository voteRepository;
    private final CommentMapper commentMapper;


    public BookmarkServiceImpl(JpaBookmarkRepository bookmarkRepository,
                               JpaAnswerRepository answerRepository,
                               JpaUserRepository userRepository,
                               JpaQuestionRepository questionRepository,
                               BookmarkMapper bookmarkMapper,
                               QuestionMapper questionMapper,
                               AnswerMapper answerMapper,
                               JpaFollowRepository followRepository,
                               JpaVoteRepository voteRepository,
                               CommentMapper commentMapper) {
        this.bookmarkRepository = bookmarkRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.bookmarkMapper = bookmarkMapper;
        this.questionMapper = questionMapper;
        this.answerMapper = answerMapper;
        this.followRepository = followRepository;
        this.voteRepository = voteRepository;
        this.commentMapper = commentMapper;
    }


    @Override
    @Transactional
    public BookmarkResponse bookmarkQuestion(BookmarkQuestionRequest bookmarkRequest) {
        JpaBookmark jpaBookmark = bookmarkRepository.findByUserIdAndJpaQuestionId(bookmarkRequest.getUserId(), bookmarkRequest.getQuestionId())
                .orElse(null);

        if (jpaBookmark != null) {
            bookmarkRepository.delete(jpaBookmark);
            return null;  // If unbookmarking, return null
        }

        JpaUser jpaUser = userRepository.findById(bookmarkRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        JpaQuestion jpaQuestion = questionRepository.findById(bookmarkRequest.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        JpaBookmark newJpaBookmark = JpaBookmark.builder()
                .jpaUser(jpaUser)
                .jpaQuestion(jpaQuestion)
                .createdAt(LocalDateTime.now())
                .build();

        JpaBookmark savedJpaBookmark = bookmarkRepository.save(newJpaBookmark);
        return bookmarkMapper.toResponse(savedJpaBookmark);

    }

    @Override
    @Transactional
    public BookmarkResponse bookmarkAnswer(BookmarkAnswerRequest bookmarkRequest) {
        try {
            JpaBookmark jpaBookmark = bookmarkRepository.findByUserIdAndJpaAnswerId(bookmarkRequest.getUserId(), bookmarkRequest.getAnswerId())
                    .orElse(null);
            if (jpaBookmark != null) {
                bookmarkRepository.delete(jpaBookmark);
                return null;
            }else {
                JpaUser jpaUser = userRepository.findById(bookmarkRequest.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                JpaAnswer jpaAnswer = answerRepository.findById(bookmarkRequest.getAnswerId())
                        .orElseThrow(() -> new RuntimeException("Answer not found"));

                JpaBookmark newJpaBookmark = JpaBookmark.builder()
                        .jpaUser(jpaUser)
                        .jpaAnswer(jpaAnswer)
                        .createdAt(LocalDateTime.now())
                        .build();

                JpaBookmark savedJpaBookmark = bookmarkRepository.save(newJpaBookmark);
                return bookmarkMapper.toResponse(savedJpaBookmark);
            }

        } catch (Exception e) {
            throw new BookmarkAlreadyExistsException("Bookmark already exists");
        }
    }

    @Override
    @Cacheable(value = "userBookmarkedQuestions", key = "#userId + '-' + #pageable.pageNumber")
    public Page<QuestionResponse> getUserBookmarkedQuestions(Long userId, Pageable pageable) {
        Page<JpaBookmark> jpaBookmarks = bookmarkRepository.findByUserIdAndJpaQuestionIsNotNull(userId, pageable);
        return jpaBookmarks.map(bookmark ->
                questionMapper.toResponse(bookmark.getJpaQuestion())
        );
    }

    @Override
    @Cacheable(value = "userBookmarkedAnswers", key = "#userId + '-' + #pageable.pageNumber")
    public Page<AnswerResponse> getUserBookmarkedAnswers(Long userId, Pageable pageable) {
        Page<JpaBookmark> jpaBookmarks = bookmarkRepository.findByUserIdAndJpaAnswerIsNotNull(userId, pageable);
        return jpaBookmarks.map(bookmark ->
                answerMapper.toResponse(bookmark.getJpaAnswer(), userId, bookmarkRepository, followRepository, voteRepository, commentMapper)
        );
    }

}
