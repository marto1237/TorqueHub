package torquehub.torquehub.persistence.repository;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.Tag;

import java.util.List;
import java.util.Optional;


@Repository
public interface TagRepository {

    Tag save(Tag tag);
    Optional<Tag> findById(Long tagId);
    Optional<Tag> findByName(String tagName);
    boolean delete(Tag tag);
    boolean existsById(Long tagId);
    List<Tag> findAll();
}
