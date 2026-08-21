package com.idp.developer.initializer;

public class Initializer {

    private Initializer() {
    }

    public static void init(InitializerBean initializerBean) {
        ClaimInitializer.init(initializerBean);
        ScopeInitializer.init(initializerBean);
        AuthenticationMethodsInitializer.init(initializerBean);
        GrantTypeInitializer.init(initializerBean);
        RoleInitializer.init(initializerBean);
        GroupInitializer.init(initializerBean);
        AuthorizationServerInitializer.init(initializerBean);
        ClientInitializer.init(initializerBean);
        UserInitializer.init(initializerBean);
    }
}
