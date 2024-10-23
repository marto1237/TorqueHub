package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;

@Mapper(componentModel = "spring")
public interface ReputationMapper {

    @Mapping(target = "userId", source = "jpaUser.id")
    @Mapping(target = "updatedReputationPoints", source = "jpaUser.points")
    @Mapping(target = "message", source = "message")
    ReputationResponse toResponse(JpaUser jpaUser, String message, int points);

}
