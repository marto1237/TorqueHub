package torquehub.torquehub.business.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.exeption.BookmarkAlreadyExistsException;
import torquehub.torquehub.business.interfaces.BookmarkService;
import torquehub.torquehub.domain.mapper.BookmarkMapper;
import torquehub.torquehub.domain.model.*;
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
    public BookmarkResponse bookmarkQuestion(BookmarkRequest bookmarkRequest) {
        try {
            Bookmark bookmark = bookmarkRepository.findByUserIdAndQuestionId(bookmarkRequest.getUserId(), bookmarkRequest.getQuestionId())
                    .orElse(null);
            if (bookmark != null) {
                bookmarkRepository.delete(bookmark);
                return null;
            }else {
                User user = userRepository.findById(bookmarkRequest.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                Question question = questionRepository.findById(bookmarkRequest.getQuestionId())
                        .orElseThrow(() -> new RuntimeException("Question not found"));

                Bookmark newBookmark = Bookmark.builder()
                        .user(user)
                        .question(question)
                        .createdAt(LocalDateTime.now())
                        .build();

                Bookmark savedBookmark = bookmarkRepository.save(newBookmark);
                return bookmarkMapper.toResponse(savedBookmark);
            }

        } catch (Exception e) {
            throw new BookmarkAlreadyExistsException("Bookmark already exists");
        }

    }

    @Override
    @Transactional
    public BookmarkResponse bookmarkAnswer(BookmarkRequest bookmarkRequest) {
        try {
            Bookmark bookmark = bookmarkRepository.findByUserIdAndAnswerId(bookmarkRequest.getUserId(), bookmarkRequest.getAnswerId())
                    .orElse(null);
            if (bookmark != null) {
                bookmarkRepository.delete(bookmark);
                return null;
            }else {
                User user = userRepository.findById(bookmarkRequest.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                Answer answer = answerRepository.findById(bookmarkRequest.getAnswerId())
                        .orElseThrow(() -> new RuntimeException("Answer not found"));

                Bookmark newBookmark = Bookmark.builder()
                        .user(user)
                        .answer(answer)
                        .createdAt(LocalDateTime.now())
                        .build();

                Bookmark savedBookmark = bookmarkRepository.save(newBookmark);
                return bookmarkMapper.toResponse(savedBookmark);
            }

        } catch (Exception e) {
            throw new BookmarkAlreadyExistsException("Bookmark already exists");
        }
    }



    @Override
    public Optional<List<BookmarkResponse>> getUserBookmarks(Long userId) {
        List<Bookmark> bookmarks = bookmarkRepository.findByUserId(userId);
        if (bookmarks.isEmpty()) {
            return Optional.empty();
        }else {
            return Optional.of(bookmarks.stream()
                    .map(bookmarkMapper::toResponse)
                    .toList());
        }
    }

}
