package torquehub.torquehub.business.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.exeption.BookmarkAlreadyExistsException;
import torquehub.torquehub.business.interfaces.BookmarkService;
import torquehub.torquehub.domain.mapper.BookmarkMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaAnswer;
import torquehub.torquehub.domain.model.jpa_models.JpaBookmark;
import torquehub.torquehub.domain.model.jpa_models.JpaQuestion;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.request.BookmarkDtos.BookmarkQuestionRequest;
import torquehub.torquehub.domain.request.BookmarkDtos.BookmarkRequest;
import torquehub.torquehub.domain.response.BookmarkDtos.BookmarkResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaAnswerRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaBookmarkRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaQuestionRepository;
import torquehub.torquehub.persistence.jpa.impl.JpaUserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    private final JpaBookmarkRepository bookmarkRepository;
    private final JpaAnswerRepository answerRepository;
    private final JpaUserRepository userRepository;
    private final JpaQuestionRepository questionRepository;
    private final BookmarkMapper bookmarkMapper;

    public BookmarkServiceImpl(JpaBookmarkRepository bookmarkRepository,
                               JpaAnswerRepository answerRepository,
                               JpaUserRepository userRepository,
                               JpaQuestionRepository questionRepository,
                               BookmarkMapper bookmarkMapper) {
        this.bookmarkRepository = bookmarkRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.bookmarkMapper = bookmarkMapper;
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
    public BookmarkResponse bookmarkAnswer(BookmarkRequest bookmarkRequest) {
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
    public Optional<List<BookmarkResponse>> getUserBookmarks(Long userId) {
        List<JpaBookmark> jpaBookmarks = bookmarkRepository.findByUserId(userId);
        if (jpaBookmarks.isEmpty()) {
            return Optional.empty();
        }else {
            return Optional.of(jpaBookmarks.stream()
                    .map(bookmarkMapper::toResponse)
                    .toList());
        }
    }

}
