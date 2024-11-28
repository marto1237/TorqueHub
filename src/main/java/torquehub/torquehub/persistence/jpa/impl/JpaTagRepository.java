package torquehub.torquehub.persistence.jpa.impl;

import org.springframework.stereotype.Repository;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;
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
    public JpaTag save(JpaTag jpaTag) {
        return tagRepository.save(jpaTag);
    }

    @Override
    public Optional<JpaTag> findById(Long tagId) {
        return tagRepository.findById(tagId);
    }

    @Override
    public Optional<JpaTag> findByName(String tagName) {
        return tagRepository.findByName(tagName);
    }

    @Override
    public boolean delete(JpaTag jpaTag) {
        if (tagRepository.existsById(jpaTag.getId())) {
            tagRepository.delete(jpaTag);
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
    public List<JpaTag> findAll() {
        return tagRepository.findAll();
    }

    @Override
    public List<JpaTag> findTop5ByOrderByUsageCountDesc() {
        return tagRepository.findTop5ByOrderByUsageCountDesc();
    }

    @Override
    public List<JpaTag> findTop5ByNameContainingIgnoreCaseOrderByUsageCountDesc(String searchQuery) {
        return tagRepository.findTop5ByNameContainingIgnoreCaseOrderByUsageCountDesc(searchQuery);
    }

    @Override
    public List<JpaTag> findByNameContainingIgnoreCase(String name) {
        return tagRepository.findByNameContainingIgnoreCase(name);
    }

}
