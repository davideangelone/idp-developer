package com.idp.developer.mapper;

import com.idp.developer.entity.OAuth2AuthenticationMethod;
import com.idp.developer.entity.OAuth2Client;
import com.idp.developer.entity.OAuth2GrantType;
import com.idp.developer.entity.OAuth2Scope;
import com.idp.developer.model.OAuth2ClientDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OAuth2ClientDtoMapper {
    OAuth2ClientDto toDto(OAuth2Client entity);

    default String map(OAuth2AuthenticationMethod authenticationMethod) {
        return authenticationMethod.getName();
    }

    default String map(OAuth2Scope scope) {
        return scope.getName();
    }

    default String map(OAuth2GrantType grantType) {
        return grantType.getName();
    }
}
