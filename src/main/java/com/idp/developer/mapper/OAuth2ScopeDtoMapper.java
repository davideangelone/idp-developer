package com.idp.developer.mapper;

import com.idp.developer.entity.OAuth2Scope;
import com.idp.developer.model.OAuth2ScopeDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OAuth2ScopeDtoMapper {
    OAuth2ScopeDto toDto(OAuth2Scope entity);
}
