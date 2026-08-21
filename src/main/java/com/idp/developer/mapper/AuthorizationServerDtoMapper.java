package com.idp.developer.mapper;

import com.idp.developer.entity.AuthorizationServer;
import com.idp.developer.entity.Group;
import com.idp.developer.entity.OAuth2AuthenticationMethod;
import com.idp.developer.entity.OAuth2GrantType;
import com.idp.developer.entity.OAuth2Scope;
import com.idp.developer.entity.Role;
import com.idp.developer.model.AuthorizationServerDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthorizationServerDtoMapper {

    AuthorizationServerDto toDto(AuthorizationServer entity);

    default String map(OAuth2AuthenticationMethod authenticationMethod) {
        return authenticationMethod.getName();
    }

    default String map(OAuth2Scope scope) {
        return scope.getName();
    }

    default String map(OAuth2GrantType grantType) {
        return grantType.getName();
    }

    default String map(Role role) { return role.getName(); }

    default String map(Group group) { return group.getName(); }
}