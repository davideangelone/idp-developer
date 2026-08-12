package com.idp.enterpriseidp.properties;

import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String issuerUrl;
    private boolean authorizationConsent;
    private JwtProperties jwt;
    private List<UserProperties> users;
}