package torquehub.torquehub.persistence.jpa.impl;
import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.Bookmark;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaBookmarkRepository;
import torquehub.torquehub.persistence.repository.BookmarkRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaBookmarkRepository implements BookmarkRepository {

    private final SpringDataJpaBookmarkRepository bookmarkRepository;

    public JpaBookmarkRepository(SpringDataJpaBookmarkRepository bookmarkRepository) {
        this.bookmarkRepository = bookmarkRepository;
    }

    @Override
    public Bookmark save(Bookmark bookmark) {
        return bookmarkRepository.save(bookmark);
    }

    @Override
    public Optional<Bookmark> findById(Long id) {
        return bookmarkRepository.findById(id);
    }

    @Override
    public List<Bookmark> findByUserId(Long userId) {
        return bookmarkRepository.findByUserId(userId);
    }

    @Override
    public Optional<Bookmark> findByUserIdAndQuestionId(Long userId, Long questionId) {
        return bookmarkRepository.findByUserIdAndQuestionId(userId, questionId);
    }

    @Override
    public Optional<Bookmark> findByUserIdAndAnswerId(Long userId, Long answerId) {
        return bookmarkRepository.findByUserIdAndAnswerId(userId, answerId);
    }

    @Override
    public boolean delete(Bookmark bookmark) {
        if (bookmarkRepository.existsById(bookmark.getId())) {
            bookmarkRepository.delete(bookmark);
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void deleteById(Long id) {
        bookmarkRepository.deleteById(id);
    }
}

