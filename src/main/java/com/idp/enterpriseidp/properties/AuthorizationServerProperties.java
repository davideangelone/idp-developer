package com.idp.enterpriseidp.properties;

import java.time.Duration;
import java.util.List;

import lombok.Data;

@Data
public class AuthorizationServerProperties {
    private String issuerUrl;
    private boolean authorizationConsent;
    private Duration accessTokenTtl = Duration.ofMinutes(5);
    private Duration refreshTokenTtl = Duration.ofDays(30);
    private Duration authorizationCodeTtl = Duration.ofMinutes(5);
    private boolean reuseRefreshTokens;
    private List<String> supportedScopes;
}
