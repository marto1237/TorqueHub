package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.model.jpa_models.JpaUser;
import torquehub.torquehub.domain.request.UserDtos.UserCreateRequest;
import torquehub.torquehub.domain.request.UserDtos.UserUpdateRequest;
import torquehub.torquehub.domain.response.UserDtos.UserResponse;

@Mapper(componentModel = "spring", uses = {RoleMapperHelper.class})
public interface UserMapper {


    @Mapping(target = "password", ignore = true)
    @Mapping(source = "role", target = "jpaRole", qualifiedByName = "stringToRole")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "salt", ignore = true)
    @Mapping(target = "points", ignore = true)
    JpaUser toEntity(UserCreateRequest userCreateRequest);

    @Mapping(source = "email", target = "jpaRole", qualifiedByName = "stringToRole")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "salt", ignore = true)
    @Mapping(target = "points", ignore = true)
    JpaUser toEntity(UserUpdateRequest userUpdateRequest);

    @Mapping(source = "jpaRole.name", target = "role")
    @Mapping(target = "points", source = "points")
    UserResponse toResponse(JpaUser jpaUser);
}

