package com.idp.developer.initializer;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.idp.developer.entity.Group;
import com.idp.developer.entity.Role;
import com.idp.developer.entity.User;

public class UserInitializer {

    private UserInitializer() {
    }

    public static void init(InitializerBean initializerBean) {

        initializerBean.getConfigProperties().getUsers().forEach(userProperties -> {

            Set<Role> userRoles = userProperties.getRoles().stream()
                    .map(role -> getRole(initializerBean.getRoles(), role, userProperties.getUsername()))
                    .collect(Collectors.toSet());

            Set<Group> userGroups = userProperties.getGroups().stream()
                    .map(group -> getGroup(initializerBean.getGroups(), group, userProperties.getUsername()))
                    .collect(Collectors.toSet());

            User user = initializerBean.getUserRepository()
                    .findByUsername(userProperties.getUsername())
                    .orElseGet(User::new);

            user.setUsername(userProperties.getUsername());
            user.setPassword(initializerBean.getPasswordEncoder().encode(userProperties.getPassword()));
            user.setFirstName(userProperties.getFirstName());
            user.setLastName(userProperties.getLastName());
            user.setEmail(userProperties.getEmail());
            user.setAddress(userProperties.getAddress());
            user.setPhoneNumber(userProperties.getPhoneNumber());
            user.setRoles(userRoles);
            user.setGroups(userGroups);
            user.setEmailVerified(true);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);
            user.setEnabled(true);

            initializerBean.getUserRepository().save(user);
        });
    }

    private static Role getRole(Map<String, Role> roles, String name, String username) {
        Role role = roles.get(name);
        if (role == null) {
            throw new IllegalStateException("Role [" + name + "] non valido per l'utente [" + username + "], valori validi " + roles.keySet());
        }

        return role;
    }

    private static Group getGroup(Map<String, Group> groups, String name, String username) {
        Group group = groups.get(name);
        if (group == null) {
            throw new IllegalStateException("Group [" + name + "] non valido per l'utente [" + username + "], valori validi " + groups.keySet());
        }

        return group;
    }
}
