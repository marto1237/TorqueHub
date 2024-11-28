package torquehub.torquehub.business.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.exeption.tag_exeptions.DuplicateTagException;
import torquehub.torquehub.business.exeption.tag_exeptions.TagDeleteExeption;
import torquehub.torquehub.business.exeption.tag_exeptions.TagNotFoundException;
import torquehub.torquehub.business.exeption.tag_exeptions.TageCreationExeption;
import torquehub.torquehub.business.interfaces.TagService;
import torquehub.torquehub.domain.mapper.TagMapper;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;
import torquehub.torquehub.domain.request.tag_dtos.TagCreateRequest;
import torquehub.torquehub.domain.request.tag_dtos.TagUpdateRequest;
import torquehub.torquehub.domain.response.tag_dtos.TagResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaTagRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TagServiceImpl implements TagService {

    private  final JpaTagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagServiceImpl(JpaTagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }


    @Override
    @Cacheable(value = "allTags")
    public List<TagResponse> getAllTags() {
        return  tagRepository.findAll().stream()
                .map(tagMapper::toResponse)
                .toList();
    }

    private static final String TAG_ID_PREFIX = "Tag with ID ";
    private static final String NOT_FOUND_SUFFIX = " not found";

    @Override
    @CacheEvict(value = {"allTags", "tagById"}, key = "#id", allEntries = true)
    @Transactional
    public TagResponse createTag(TagCreateRequest tagCreateRequest) {
        try{
            Optional<JpaTag> existingTag = tagRepository.findByName(tagCreateRequest.getName());
            if (existingTag.isPresent()) {
                throw new DuplicateTagException("Tag with name '" + tagCreateRequest.getName() + "' already exists.");
            }else {
                JpaTag jpaTag = JpaTag.builder()
                        .usageCount(0)
                        .name(tagCreateRequest.getName()).build();
                JpaTag createdJpaTag = tagRepository.save(jpaTag);
                return tagMapper.toResponse(createdJpaTag);
            }
        }
        catch (Exception e){
            throw new TageCreationExeption("Failed to create tag: " + e.getMessage(),e);
        }

    }

    @Override
    @Cacheable(value = "tagById", key = "#id")
    public Optional<TagResponse> getTagById(Long id) {
        return tagRepository.findById(id).map(tagMapper::toResponse);
    }

    @Override
    @CacheEvict(value = {"allTags", "tagById"}, key = "#id", allEntries = true)
    public boolean deleteTag(Long id) {
        try{
            Optional<JpaTag> tagOptional = tagRepository.findById(id);
            if(tagOptional.isPresent()){
                JpaTag jpaTag = tagOptional.get();
                tagRepository.delete(jpaTag);
                return true;
            }else {
                throw new IllegalArgumentException(TAG_ID_PREFIX + id + NOT_FOUND_SUFFIX);
            }

        }catch (Exception e){
            throw new TagDeleteExeption("Failed to delete tag: " + e.getMessage(),e);
        }
    }

    @Override
    public boolean tagExistsById(Long id) {
        return tagRepository.existsById(id);
    }

    @Override
    @Transactional
    public boolean updateTagById(Long id, TagUpdateRequest tagUpdateRequest) {
        try {
            Optional<JpaTag> tagOptional = tagRepository.findById(id);
            if (tagOptional.isPresent()) {
                JpaTag existingJpaTag = tagOptional.get();
                existingJpaTag.setName(tagUpdateRequest.getName());
                tagRepository.save(existingJpaTag);
                return true;
            } else {
                throw new IllegalArgumentException(TAG_ID_PREFIX + id + NOT_FOUND_SUFFIX);
            }
        } catch (Exception e) {
            throw new TagNotFoundException(TAG_ID_PREFIX + id + NOT_FOUND_SUFFIX, e);
        }

    }

    @Override
    public List<TagResponse> getTop5Tags(String searchQuery) {
        List<JpaTag> tags;

        if (searchQuery == null || searchQuery.isEmpty()) {
            // Fetch the top 5 most-used tags without any filter
            tags = tagRepository.findTop5ByOrderByUsageCountDesc();
        } else {
            // Fetch the top 5 most-used tags that match the search query
            tags = tagRepository.findTop5ByNameContainingIgnoreCaseOrderByUsageCountDesc(searchQuery);
        }

        // Map JpaTag entities to TagResponse DTOs
        return tags.stream()
                .map(tagMapper::toResponse)
                .toList();
    }

    @Override
    public List<TagResponse> findTagsByName(String name) {
        return tagRepository.findByNameContainingIgnoreCase(name).stream()
                .map(tagMapper::toResponse)
                .toList();
    }


}
