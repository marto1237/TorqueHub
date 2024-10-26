package torquehub.torquehub.business.interfaces;

import torquehub.torquehub.domain.request.tag_dtos.TagCreateRequest;
import torquehub.torquehub.domain.request.tag_dtos.TagUpdateRequest;
import torquehub.torquehub.domain.response.tag_dtos.TagResponse;

import java.util.List;
import java.util.Optional;

public interface TagService {

    List<TagResponse> getAllTags();

    TagResponse createTag(TagCreateRequest tagCreateRequest);

    Optional<TagResponse> getTagById(Long id);

    boolean deleteTag(Long id);

    boolean tagExistsById(Long id);

    boolean updateTagById(Long id, TagUpdateRequest tagUpdateRequest);
}
