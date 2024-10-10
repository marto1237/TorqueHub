package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.Tag;
import torquehub.torquehub.persistence.jpa.interfaces.SpringDataJpaTagRepository;
import torquehub.torquehub.persistence.repository.TagRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaTagRepository implements TagRepository {

    private final SpringDataJpaTagRepository tagRepository;

    public JpaTagRepository(SpringDataJpaTagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public Tag save(Tag tag) {
        return tagRepository.save(tag);
    }

    @Override
    public Optional<Tag> findById(Long tagId) {
        return tagRepository.findById(tagId);
    }

    @Override
    public Optional<Tag> findByName(String tagName) {
        return tagRepository.findByName(tagName);
    }

    @Override
    public boolean delete(Tag tag) {
        if (tagRepository.existsById(tag.getId())) {
            tagRepository.delete(tag);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean existsById(Long tagId) {
        return tagRepository.existsById(tagId);
    }

    @Override
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }
}
