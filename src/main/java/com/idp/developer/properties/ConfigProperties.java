package com.idp.developer.properties;

import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties
public class ConfigProperties {
    private AuthorizationServerProperties authorizationServer;
    private JwtProperties jwt;
    private List<OAuth2ClientProperties> oauth2Clients;
    private ClaimsProperties claims;
    private List<UserProperties> users;
}