package com.idp.developer.initializer;

import java.util.HashMap;
import java.util.Map;

import com.idp.developer.entity.OAuth2GrantType;

public class GrantTypeInitializer {

    private GrantTypeInitializer() {
    }

    public static void init(InitializerBean initializerBean) {

        Map<String, OAuth2GrantType> grantTypes = new HashMap<>();

        for (java.lang.String name : initializerBean.getConfigProperties().getAuthorizationServer().getSupportedGrantTypes()) {
            OAuth2GrantType grantType = initializerBean.getOAuth2GrantTypeRepository().findByName(name);
            if (null == grantType) {
                grantType = new OAuth2GrantType();
                grantType.setName(name);
                grantType = initializerBean.getOAuth2GrantTypeRepository().save(grantType);
            }
            grantTypes.put(grantType.getName(), grantType);
        }

        initializerBean.setGrantTypes(grantTypes);
    }

}
