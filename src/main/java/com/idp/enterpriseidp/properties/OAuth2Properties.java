package com.idp.enterpriseidp.properties;

import lombok.Data;

@Data
public class OAuth2Properties {
    private String clientUrl;
    private String clientId;
    private String clientSecret;
    private String redirectUrlClient;
    private String redirectUrlTest;
    private String postLogoutRedirectUrl;
}