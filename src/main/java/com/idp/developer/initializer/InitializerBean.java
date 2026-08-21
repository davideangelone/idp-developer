package com.idp.developer.initializer;

import java.util.Map;

import com.idp.developer.entity.Group;
import com.idp.developer.entity.OAuth2AuthenticationMethod;
import com.idp.developer.entity.OAuth2GrantType;
import com.idp.developer.entity.OAuth2Scope;
import com.idp.developer.entity.Role;
import com.idp.developer.properties.ConfigProperties;
import com.idp.developer.repository.AuthorizationServerRepository;
import com.idp.developer.repository.GroupRepository;
import com.idp.developer.repository.OAuth2AuthenticationMethodRepository;
import com.idp.developer.repository.OAuth2ClaimRepository;
import com.idp.developer.repository.OAuth2ClientRepository;
import com.idp.developer.repository.OAuth2GrantTypeRepository;
import com.idp.developer.repository.OAuth2ScopeRepository;
import com.idp.developer.repository.RoleRepository;
import com.idp.developer.repository.UserRepository;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
public class InitializerBean {
    private Map<String, OAuth2AuthenticationMethod> authenticationMethods;
    private Map<String, OAuth2Scope> scopes;
    private Map<String, OAuth2GrantType> grantTypes;
    private Map<String, Role> roles;
    private Map<String, Group> groups;
    private AuthorizationServerRepository authorizationServerRepository;
    private OAuth2ClaimRepository oAuth2ClaimRepository;
    private OAuth2ScopeRepository oAuth2ScopeRepository;
    private OAuth2AuthenticationMethodRepository oAuth2AuthenticationMethodRepository;
    private OAuth2GrantTypeRepository oAuth2GrantTypeRepository;
    private OAuth2ClientRepository oAuth2ClientRepository;
    private RoleRepository roleRepository;
    private GroupRepository groupRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private ConfigProperties configProperties;

    public Map<String, OAuth2AuthenticationMethod> getAuthenticationMethods() {
        if (null == authenticationMethods) {
            throw new IllegalStateException("Authentication methods non inizializzati");
        }
        return authenticationMethods;
    }

    public Map<String, OAuth2Scope> getScopes() {
        if (null == scopes) {
            throw new IllegalStateException("Scopes non inizializzati");
        }
        return scopes;
    }

    public Map<String, OAuth2GrantType> getGrantTypes() {
        if (null == grantTypes) {
            throw new IllegalStateException("Grant types non inizializzati");
        }
        return grantTypes;
    }

    public Map<String, Role> getRoles() {
        if (null == roles) {
            throw new IllegalStateException("Roles non inizializzati");
        }
        return roles;
    }

    public Map<String, Group> getGroups() {
        if (null == groups) {
            throw new IllegalStateException("Groups non inizializzati");
        }
        return groups;
    }
}
