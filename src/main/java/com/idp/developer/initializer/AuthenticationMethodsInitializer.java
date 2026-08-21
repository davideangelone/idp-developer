package com.idp.developer.initializer;

import java.util.HashMap;
import java.util.Map;

import com.idp.developer.entity.OAuth2AuthenticationMethod;

public class AuthenticationMethodsInitializer {

    private AuthenticationMethodsInitializer() {
    }

    public static void init(InitializerBean initializerBean) {

        Map<String, OAuth2AuthenticationMethod> authenticationMethods = new HashMap<>();

        for (String name : initializerBean.getConfigProperties().getAuthorizationServer().getSupportedAuthenticationMethods()) {

            OAuth2AuthenticationMethod authenticationMethod = initializerBean.getOAuth2AuthenticationMethodRepository().findByName(name);
            if (null == authenticationMethod) {
                authenticationMethod = new OAuth2AuthenticationMethod();
                authenticationMethod.setName(name);
                authenticationMethod = initializerBean.getOAuth2AuthenticationMethodRepository().save(authenticationMethod);
            }

            authenticationMethods.put(authenticationMethod.getName(), authenticationMethod);
        }

        initializerBean.setAuthenticationMethods(authenticationMethods);
    }
}
