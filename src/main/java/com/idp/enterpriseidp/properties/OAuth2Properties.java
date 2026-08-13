package com.idp.enterpriseidp.properties;

import java.util.List;

import lombok.Data;

@Data
public class OAuth2Properties {
    private String clientUrl;
    private String clientId;
    private String clientSecret;
    private List<String> redirectUris;
    private List<String> postLogoutRedirectUris;
    private List<String> scopes;
}