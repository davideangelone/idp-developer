package com.idp.developer.properties;

import java.util.List;
import java.util.Map;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties
public class ConfigProperties {
    private AuthorizationServerProperties authorizationServer;
    private JwtProperties jwt;
    private Map<String, OAuth2ClientProperties> oauth2Clients;
    private ClaimsProperties claims;
    private List<UserProperties> users;
}