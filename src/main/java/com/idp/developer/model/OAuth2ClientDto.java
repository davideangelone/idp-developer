package com.idp.developer.model;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public record OAuth2ClientDto(
        Long id,
        String name,
        String clientId,
        String clientSecret,
        Set<String> clientAuthenticationMethods,
        String clientUrl,
        List<String> redirectUris,
        List<String> postLogoutRedirectUris,
        Set<String> scopes,
        Set<String> authorizationGrantTypes,
        boolean authorizationConsent,
        boolean requireProofKey,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        Duration authorizationCodeTtl,
        boolean reuseRefreshTokens
) {
}
