package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import torquehub.torquehub.domain.model.Tag;
import torquehub.torquehub.domain.request.TagDtos.TagCreateRequest;
import torquehub.torquehub.domain.request.TagDtos.TagUpdateRequest;
import torquehub.torquehub.domain.response.TagDtos.TagResponse;

@Mapper(componentModel = "spring")
public interface TagMapper {

    Tag toEntity(TagCreateRequest tagCreateRequest);
    Tag toEntity(TagUpdateRequest tagUpdateRequest);
    TagResponse toResponse(Tag tag);
}
