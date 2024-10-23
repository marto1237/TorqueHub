package torquehub.torquehub.persistence.jpa.impl;
import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.jpa_models.JpaBookmark;
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
    public JpaBookmark save(JpaBookmark jpaBookmark) {
        return bookmarkRepository.save(jpaBookmark);
    }

    @Override
    public Optional<JpaBookmark> findById(Long id) {
        return bookmarkRepository.findById(id);
    }

    @Override
    public List<JpaBookmark> findByUserId(Long userId) {
        return bookmarkRepository.findByJpaUserId(userId);
    }

    @Override
    public Optional<JpaBookmark> findByUserIdAndJpaQuestionId(Long userId, Long questionId) {
        return bookmarkRepository.findByJpaUserIdAndJpaQuestionId(userId, questionId);
    }

    @Override
    public Optional<JpaBookmark> findByUserIdAndJpaAnswerId(Long userId, Long answerId) {
        return bookmarkRepository.findByJpaUserIdAndJpaAnswerId(userId, answerId);
    }

    @Override
    public boolean delete(JpaBookmark jpaBookmark) {
        if (bookmarkRepository.existsById(jpaBookmark.getId())) {
            bookmarkRepository.delete(jpaBookmark);
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

