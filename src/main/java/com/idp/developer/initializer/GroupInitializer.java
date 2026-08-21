package com.idp.developer.initializer;

import java.util.HashMap;
import java.util.Map;

import com.idp.developer.entity.Group;

public class GroupInitializer {

    private GroupInitializer() {
    }

    public static void init(InitializerBean initializerBean) {

        Map<String, Group> groups = new HashMap<>();

        for (String name : initializerBean.getConfigProperties().getAuthorizationServer().getSupportedGroups()) {
            Group group = initializerBean.getGroupRepository().findByName(name);
            if (null == group) {
                group = new Group();
                group.setName(name);
                group = initializerBean.getGroupRepository().save(group);
            }
            groups.put(group.getName(), group);
        }

        initializerBean.setGroups(groups);
    }
}
