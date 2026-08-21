package com.idp.developer.initializer;

import java.util.stream.Collectors;

import com.idp.developer.entity.AuthorizationServer;

public class AuthorizationServerInitializer {

    private AuthorizationServerInitializer() {
    }

    public static void init(InitializerBean initializerBean) {

        var properties = initializerBean.getConfigProperties().getAuthorizationServer();

        AuthorizationServer authorizationServer = initializerBean.getAuthorizationServerRepository()
                .findFirstByOrderByIdAsc()
                .orElseGet(AuthorizationServer::new);

        authorizationServer.setIssuerUrl(properties.getIssuerUrl());
        authorizationServer.setFreeLogin(properties.isFreeLogin());

        authorizationServer.setSupportedAuthenticationMethods(
                properties.getSupportedAuthenticationMethods().stream()
                        .map(authenticationMethod -> initializerBean.getAuthenticationMethods().get(authenticationMethod))
                        .collect(Collectors.toSet())
        );

        authorizationServer.setSupportedScopes(
                properties.getSupportedScopes().stream()
                        .map(scope -> initializerBean.getScopes().get(scope))
                        .collect(Collectors.toSet())
        );

        authorizationServer.setSupportedGrantTypes(
                properties.getSupportedGrantTypes().stream()
                        .map(grantType -> initializerBean.getGrantTypes().get(grantType))
                        .collect(Collectors.toSet())
        );

        authorizationServer.setSupportedRoles(
                properties.getSupportedRoles().stream()
                        .map(role -> initializerBean.getRoles().get(role))
                        .collect(Collectors.toSet())
        );

        authorizationServer.setSupportedGroups(
                properties.getSupportedGroups().stream()
                        .map(group -> initializerBean.getGroups().get(group))
                        .collect(Collectors.toSet())
        );

        initializerBean.getAuthorizationServerRepository().save(authorizationServer);
    }
}
