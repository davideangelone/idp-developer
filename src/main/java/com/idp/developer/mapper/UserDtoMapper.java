package com.idp.developer.mapper;

import com.idp.developer.entity.Group;
import com.idp.developer.entity.Role;
import com.idp.developer.entity.User;
import com.idp.developer.model.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserDtoMapper {
    UserDto toDto(User entity);

    default String map(Role role) {
        return role.getName();
    }

    default String map(Group group) {
        return group.getName();
    }

}
