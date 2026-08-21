package com.idp.developer.initializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.idp.developer.entity.OAuth2Claim;
import com.idp.developer.entity.OAuth2Scope;

public class ScopeInitializer {

    private ScopeInitializer() {
    }

    public static void init(InitializerBean initializerBean) {

        Map<String, OAuth2Scope> scopes = new HashMap<>();
        Map<String, Set<String>> scopeMappings = initializerBean.getConfigProperties().getClaims().getScopeMappings();

        for (String name : initializerBean.getConfigProperties().getAuthorizationServer().getSupportedScopes()) {
            Set<OAuth2Claim> claims = initializerBean.getOAuth2ClaimRepository().findByNameIn(scopeMappings.get(name));

            OAuth2Scope scope = initializerBean.getOAuth2ScopeRepository()
                    .findByName(name)
                    .orElseGet(OAuth2Scope::new);

            scope.setName(name);
            scope.setClaims(claims);
            initializerBean.getOAuth2ScopeRepository().save(scope);

            scopes.put(scope.getName(), scope);
        }

        initializerBean.setScopes(scopes);
    }
}
