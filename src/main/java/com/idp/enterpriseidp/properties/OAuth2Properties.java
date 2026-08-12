package com.idp.enterpriseidp.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Properties {
    private String clientId;
    private String clientSecret;
    private String redirectUrlClient;
    private String redirectUrlTest;
    private String postLogoutRedirectUrl;
}