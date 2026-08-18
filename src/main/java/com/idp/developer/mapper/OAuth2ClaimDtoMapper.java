package com.idp.developer.mapper;

import java.util.Optional;

import com.idp.developer.entity.OAuth2Claim;
import com.idp.developer.entity.OAuth2Scope;
import com.idp.developer.model.OAuth2ClaimDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OAuth2ClaimDtoMapper {
    OAuth2ClaimDto toDto(OAuth2Claim entity);

    default String map(OAuth2Scope scope) {
        return Optional.ofNullable(scope).map(OAuth2Scope::getName).orElse(null);
    }
}
