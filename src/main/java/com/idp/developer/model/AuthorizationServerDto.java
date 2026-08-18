package com.idp.developer.model;

import java.time.Duration;
import java.util.Set;

public record AuthorizationServerDto(
        Long id,
        String issuerUrl,
        boolean authorizationConsent,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        Duration authorizationCodeTtl,
        boolean reuseRefreshTokens,
        Set<String> supportedScopes,
        Set<String> supportedGrantTypes,
        boolean freeLogin
) {
}