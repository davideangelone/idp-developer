package com.idp.developer.initializer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.idp.developer.entity.OAuth2AuthenticationMethod;
import com.idp.developer.entity.OAuth2Client;
import com.idp.developer.entity.OAuth2GrantType;
import com.idp.developer.entity.OAuth2Scope;
import com.idp.developer.properties.OAuth2ClientProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientInitializer {

    private ClientInitializer() {
    }

    public static void init(InitializerBean initializerBean) {

        for (OAuth2ClientProperties clientProperties : initializerBean.getConfigProperties().getOauth2Clients()) {
            OAuth2Client client = initializerBean.getOAuth2ClientRepository()
                    .findByClientId(clientProperties.getClientId())
                    .orElseGet(OAuth2Client::new);

            client.setDescription(clientProperties.getDescription());
            client.setClientId(clientProperties.getClientId());
            client.setClientSecret(clientProperties.getClientSecret());

            client.setClientAuthenticationMethods(clientProperties.getClientAuthenticationMethods().stream()
                    .map(name -> getClientAuthenticationMethod(initializerBean.getAuthenticationMethods(), name, client.getClientId()))
                    .collect(Collectors.toSet()));

            client.setClientUrl(clientProperties.getClientUrl());
            client.setRedirectUris(clientProperties.getRedirectUris());
            client.setPostLogoutRedirectUris(clientProperties.getPostLogoutRedirectUris());

            client.setScopes(clientProperties.getScopes().stream()
                    .map(name -> getScope(initializerBean.getScopes(), name, client.getClientId()))
                    .collect(Collectors.toSet()));

            client.setAuthorizationGrantTypes(clientProperties.getAuthorizationGrantTypes().stream()
                    .map(name -> getAuthorizationGrantType(initializerBean.getGrantTypes(), name, client.getClientId()))
                    .collect(Collectors.toSet()));

            client.setAuthorizationConsent(clientProperties.isAuthorizationConsent());
            client.setRequireProofKey(clientProperties.isRequireProofKey());
            client.setAccessTokenTtl(clientProperties.getAccessTokenTtl());
            client.setRefreshTokenTtl(clientProperties.getRefreshTokenTtl());
            client.setAuthorizationCodeTtl(clientProperties.getAuthorizationCodeTtl());
            client.setReuseRefreshTokens(clientProperties.isReuseRefreshTokens());

            List<String> scopesNames = client.getScopes().stream()
                    .map(OAuth2Scope::getName)
                    .toList();
            log.info("Inizializzazione client '{}' scopes: {}", client.getClientId(), scopesNames);
            initializerBean.getOAuth2ClientRepository().save(client);
        }
    }

    private static OAuth2AuthenticationMethod getClientAuthenticationMethod(Map<String, OAuth2AuthenticationMethod> authenticationMethods, String name, String clientId) {
        OAuth2AuthenticationMethod authenticationMethod = authenticationMethods.get(name);
        if (authenticationMethod == null) {
            throw new IllegalStateException("Authentication Method OAuth2 [" + name + "] non valido per il client [" + clientId + "]");
        }

        return authenticationMethod;
    }

    private static OAuth2Scope getScope(Map<String, OAuth2Scope> scopes, String name, String clientId) {
        OAuth2Scope scope = scopes.get(name);
        if (scope == null) {
            throw new IllegalStateException("Scope OAuth2 [" + name + "] non valido per il client [" + clientId + "]");
        }

        return scope;
    }

    private static OAuth2GrantType getAuthorizationGrantType(Map<String, OAuth2GrantType> grantTypes, String name, String clientId) {
        OAuth2GrantType grantType = grantTypes.get(name);
        if (grantType == null) {
            throw new IllegalStateException("Authorization Grant Type OAuth2 [" + name + "] non valido per il client [" + clientId + "]");
        }

        return grantType;
    }

}
