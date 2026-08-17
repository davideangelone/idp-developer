package com.idp.developer.properties;

import java.util.List;

import lombok.Data;

@Data
public class OAuth2ClientProperties {
    private String clientUrl;
    private String clientId;
    private String clientSecret;
    private List<String> redirectUris;
    private List<String> postLogoutRedirectUris;
    private List<String> scopes;
    private List<String> authorizationGrantTypes;
}