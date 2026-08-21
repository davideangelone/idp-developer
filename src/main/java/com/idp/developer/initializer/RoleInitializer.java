package com.idp.developer.initializer;

import java.util.HashMap;
import java.util.Map;

import com.idp.developer.entity.Role;

public class RoleInitializer {

    private RoleInitializer() {
    }

    public static void init(InitializerBean initializerBean) {

        Map<String, Role> roles = new HashMap<>();

        for (String name : initializerBean.getConfigProperties().getAuthorizationServer().getSupportedRoles()) {
            Role role = initializerBean.getRoleRepository().findByName(name);
            if (null == role) {
                role = new Role();
                role.setName(name);
                role = initializerBean.getRoleRepository().save(role);
            }
            roles.put(role.getName(), role);
        }

        initializerBean.setRoles(roles);
    }
}
