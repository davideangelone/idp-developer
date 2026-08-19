package com.idp.developer.model;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public record OAuth2ClientUpdateDto(
        Long id,
        String name,
        String clientId,
        String clientUrl,
        String clientSecret,
        List<String> redirectUris,
        List<String> postLogoutRedirectUris,
        Set<String> scopes,
        Set<String> authorizationGrantTypes,
        Set<String> clientAuthenticationMethods,
        boolean authorizationConsent,
        boolean requireProofKey,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        Duration authorizationCodeTtl,
        boolean reuseRefreshTokens
) {
}
