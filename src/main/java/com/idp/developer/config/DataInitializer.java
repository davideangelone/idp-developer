package com.idp.developer.config;

import com.idp.developer.initializer.Initializer;
import com.idp.developer.initializer.InitializerBean;
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
import com.idp.developer.service.AuthorizationServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            AuthorizationServerRepository authorizationServerRepository,
            UserRepository userRepository,
            OAuth2ClientRepository clientRepository,
            OAuth2AuthenticationMethodRepository authenticationMethodRepository,
            OAuth2ScopeRepository scopeRepository,
            OAuth2ClaimRepository claimRepository,
            OAuth2GrantTypeRepository grantTypeRepository,
            RoleRepository roleRepository,
            GroupRepository groupRepository,
            PasswordEncoder passwordEncoder,
            ConfigProperties configProperties,
            AuthorizationServerService authorizationServerService) {

        return args -> {
            InitializerBean initializerBean = new InitializerBean();
            initializerBean.setAuthorizationServerRepository(authorizationServerRepository);
            initializerBean.setOAuth2ClaimRepository(claimRepository);
            initializerBean.setOAuth2ScopeRepository(scopeRepository);
            initializerBean.setOAuth2AuthenticationMethodRepository(authenticationMethodRepository);
            initializerBean.setOAuth2GrantTypeRepository(grantTypeRepository);
            initializerBean.setOAuth2ClientRepository(clientRepository);
            initializerBean.setUserRepository(userRepository);
            initializerBean.setRoleRepository(roleRepository);
            initializerBean.setGroupRepository(groupRepository);
            initializerBean.setPasswordEncoder(passwordEncoder);
            initializerBean.setConfigProperties(configProperties);

            Initializer.init(initializerBean);

            authorizationServerService.markInitialized();
        };
    }
}