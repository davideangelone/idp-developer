package com.idp.enterpriseidp.mapper;

import com.idp.enterpriseidp.entity.User;
import com.idp.enterpriseidp.model.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserDtoMapper {
    UserDto toDto(User user);
}
