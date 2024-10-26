package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import torquehub.torquehub.domain.model.jpa_models.JpaTag;
import torquehub.torquehub.domain.request.tag_dtos.TagCreateRequest;
import torquehub.torquehub.domain.request.tag_dtos.TagUpdateRequest;
import torquehub.torquehub.domain.response.tag_dtos.TagResponse;

@Mapper(componentModel = "spring")
public interface TagMapper {

    JpaTag toEntity(TagCreateRequest tagCreateRequest);
    JpaTag toEntity(TagUpdateRequest tagUpdateRequest);
    TagResponse toResponse(JpaTag jpaTag);
}
