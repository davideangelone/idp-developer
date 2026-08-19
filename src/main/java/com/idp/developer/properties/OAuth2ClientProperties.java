package com.idp.developer.properties;

import java.time.Duration;
import java.util.List;

import lombok.Data;

@Data
public class OAuth2ClientProperties {
    private String clientId;
    private String clientSecret;
    private String description;
    private String clientUrl;
    private List<String> clientAuthenticationMethods;
    private List<String> redirectUris;
    private List<String> postLogoutRedirectUris;
    private List<String> scopes;
    private List<String> authorizationGrantTypes;
    private boolean authorizationConsent;
    private boolean requireProofKey;
    private Duration accessTokenTtl = Duration.ofMinutes(5);
    private Duration refreshTokenTtl = Duration.ofDays(30);
    private Duration authorizationCodeTtl = Duration.ofMinutes(5);
    private boolean reuseRefreshTokens;
}