package com.idp.developer.properties;

import java.util.List;

import lombok.Data;

@Data
public class AuthorizationServerProperties {
    private String issuerUrl;
    private List<String> supportedAuthenticationMethods;
    private List<String> supportedScopes;
    private List<String> supportedGrantTypes;
    private List<String> supportedRoles;
    private List<String> supportedGroups;
    private boolean freeLogin;
}
