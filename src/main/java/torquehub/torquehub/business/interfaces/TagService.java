package torquehub.torquehub.business.interfaces;

import torquehub.torquehub.domain.request.TagDtos.TagCreateRequest;
import torquehub.torquehub.domain.request.TagDtos.TagUpdateRequest;
import torquehub.torquehub.domain.response.TagDtos.TagResponse;

import java.util.List;
import java.util.Optional;

public interface TagService {

    List<TagResponse> getAllTags();

    TagResponse createTag(TagCreateRequest tagCreateRequest);

    Optional<TagResponse> getTagById(Long id);

    void deleteTag(Long id);

    boolean tagExistsById(Long id);

    boolean updateTagById(Long id, TagUpdateRequest tagUpdateRequest);
}
