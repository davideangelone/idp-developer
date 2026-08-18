package com.idp.developer.model;

import java.util.List;
import java.util.Set;

public record OAuth2ClientDto(
        Long id,
        String name,
        String clientId,
        String clientSecret,
        String clientUrl,
        List<String> redirectUris,
        List<String> postLogoutRedirectUris,
        Set<String> scopes,
        Set<String> grantTypes
) {
}
