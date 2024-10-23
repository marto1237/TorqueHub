package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;
import torquehub.torquehub.domain.request.TagDtos.TagCreateRequest;
import torquehub.torquehub.domain.request.TagDtos.TagUpdateRequest;
import torquehub.torquehub.domain.response.TagDtos.TagResponse;

@Mapper(componentModel = "spring")
public interface TagMapper {

    JpaTag toEntity(TagCreateRequest tagCreateRequest);
    JpaTag toEntity(TagUpdateRequest tagUpdateRequest);
    TagResponse toResponse(JpaTag jpaTag);
}
