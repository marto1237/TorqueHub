package torquehub.torquehub.business.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import torquehub.torquehub.business.interfaces.TagService;
import torquehub.torquehub.domain.mapper.TagMapper;
import torquehub.torquehub.domain.model.Tag;
import torquehub.torquehub.domain.request.TagDtos.TagCreateRequest;
import torquehub.torquehub.domain.request.TagDtos.TagUpdateRequest;
import torquehub.torquehub.domain.response.TagDtos.TagResponse;
import torquehub.torquehub.persistence.jpa.impl.JpaTagRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    private  final JpaTagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagServiceImpl(JpaTagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }


    @Override
    public List<TagResponse> getAllTags() {
        return  tagRepository.findAll().stream()
                .map(tagMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TagResponse createTag(TagCreateRequest tagCreateRequest) {
        Optional<Tag> existingTag = tagRepository.findByName(tagCreateRequest.getName());
        if (existingTag.isPresent()) {
            throw new IllegalArgumentException("Tag with name '" + tagCreateRequest.getName() + "' already exists.");
        }else {
            Tag tag = Tag.builder().name(tagCreateRequest.getName()).build();
            Tag createdTag = tagRepository.save(tag);
            return tagMapper.toResponse(createdTag);
        }

    }

    @Override
    public Optional<TagResponse> getTagById(Long id) {
        return tagRepository.findById(id).map(tagMapper::toResponse);
    }

    @Override
    public boolean deleteTag(Long id) {
        try{
            Optional<Tag> tagOptional = tagRepository.findById(id);
            if(tagOptional.isPresent()){
                Tag tag = tagOptional.get();
                tagRepository.delete(tag);
                return true;
            }else {
                throw new IllegalArgumentException("Tag with ID " + id + " not found.");
            }

        }catch (Exception e){
            throw new RuntimeException("Failed to delete tag: " + e.getMessage());
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
            Optional<Tag> tagOptional = tagRepository.findById(id);
            if (tagOptional.isPresent()) {
                Tag existingTag = tagOptional.get();
                existingTag.setName(tagUpdateRequest.getName());
                tagRepository.save(existingTag);
                return true;
            } else {
                throw new IllegalArgumentException("Tag with ID " + id + " not found.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Tag with ID " + id + " not found.");
        }

    }
}
