package com.idp.developer.model;

import java.util.Set;

public record AuthorizationServerDto(
        Long id,
        String issuerUrl,
        Set<String> supportedAuthenticationMethods,
        Set<String> supportedScopes,
        Set<String> supportedGrantTypes,
        Set<String> supportedRoles,
        Set<String> supportedGroups,
        boolean freeLogin
) {
}