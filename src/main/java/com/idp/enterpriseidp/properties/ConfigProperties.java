package com.idp.enterpriseidp.properties;

import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties
public class ConfigProperties {
    private AuthorizationServerProperties authorizationServer;
    private JwtProperties jwt;
    private OAuth2ClientProperties oauth2Client;
    private ClaimsProperties claims;
    private List<UserProperties> users;
}