package com.example.fincredex.mapper;

import com.example.fincredex.model.dto.UserDTO;
import com.example.fincredex.model.dto.UserProfileDTO;
import com.example.fincredex.model.entities.User;
import com.example.fincredex.model.request.RegistrationUserRequest;
import org.hibernate.type.descriptor.DateTimeUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {DateTimeUtils.class, Object.class}
)
public interface UserMapper {

    // ✅ CORRECT - MapStruct auto-maps fields with same names
    // role, username, email will map automatically from User to UserProfileDTO
    @Mapping(target = "token", source = "token")
    @Mapping(target = "refreshToken", source = "refreshToken")
    UserProfileDTO toUserProfileDTO(User user, String token, String refreshToken);

    // Or explicitly map everything:
    /*
    @Mapping(target = "role", source = "user.role")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "token", source = "token")
    @Mapping(target = "refreshToken", source = "refreshToken")
    UserProfileDTO toUserProfileDTO(User user, String token, String refreshToken);
    */

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    User fromDto(RegistrationUserRequest request);

    UserDTO toUserDTO(User user);
}
