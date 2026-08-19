package com.idp.developer.model;

import java.util.Set;

public record AuthorizationServerDto(
        Long id,
        String issuerUrl,
        Set<String> supportedScopes,
        Set<String> supportedGrantTypes,
        boolean freeLogin
) {
}