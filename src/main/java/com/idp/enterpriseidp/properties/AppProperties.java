package com.idp.enterpriseidp.properties;

import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private AuthorizationServerProperties authorizationServer;
    private JwtProperties jwt;
    private OAuth2Properties oauth2;
    private List<UserProperties> users;
}