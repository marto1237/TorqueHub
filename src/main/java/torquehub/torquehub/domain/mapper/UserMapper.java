package torquehub.torquehub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import torquehub.torquehub.domain.model.User;
import torquehub.torquehub.domain.request.UserDtos.UserCreateRequest;
import torquehub.torquehub.domain.request.UserDtos.UserUpdateRequest;
import torquehub.torquehub.domain.response.UserDtos.UserResponse;

@Mapper(componentModel = "spring", uses = {RoleMapperHelper.class})
public interface UserMapper {


    @Mapping(target = "password", ignore = true)
    @Mapping(source = "role", target = "role", qualifiedByName = "stringToRole")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "salt", ignore = true)
    User toEntity(UserCreateRequest userCreateRequest);

    @Mapping(source = "email", target = "role", qualifiedByName = "stringToRole")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "salt", ignore = true)
    User toEntity(UserUpdateRequest userUpdateRequest);

    @Mapping(source = "role.name", target = "role")
    UserResponse toResponse(User user);
}

