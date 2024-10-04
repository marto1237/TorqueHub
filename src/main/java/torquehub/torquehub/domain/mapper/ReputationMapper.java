package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.response.ReputationDtos.ReputationResponse;

@Mapper(componentModel = "spring")
public interface ReputationMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "updatedReputationPoints", source = "user.points")
    @Mapping(target = "message", source = "message")
    ReputationResponse toResponse(User user, String message, int points);
}
