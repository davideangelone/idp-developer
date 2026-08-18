package com.idp.developer.config;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.idp.developer.entity.AuthorizationServer;
import com.idp.developer.entity.OAuth2Client;
import com.idp.developer.entity.OAuth2GrantType;
import com.idp.developer.entity.OAuth2Scope;
import com.idp.developer.entity.User;
import com.idp.developer.properties.ConfigProperties;
import com.idp.developer.properties.OAuth2ClientProperties;
import com.idp.developer.repository.AuthorizationServerRepository;
import com.idp.developer.repository.OAuth2ClientRepository;
import com.idp.developer.repository.OAuth2GrantTypeRepository;
import com.idp.developer.repository.OAuth2ScopeRepository;
import com.idp.developer.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            AuthorizationServerRepository authorizationServerRepository,
            UserRepository userRepository,
            OAuth2ClientRepository clientRepository,
            OAuth2ScopeRepository scopeRepository,
            OAuth2GrantTypeRepository grantTypeRepository,
            PasswordEncoder passwordEncoder,
            ConfigProperties configProperties) {

        Map<String, OAuth2Scope> scopes = initScopes(scopeRepository, configProperties);
        Map<String, OAuth2GrantType> grantTypes = initGrantTypes(grantTypeRepository, configProperties);

        return args -> {
            initAuthorizationServer(scopes, grantTypes, authorizationServerRepository, configProperties);
            initUsers(userRepository, passwordEncoder, configProperties);
            initClients(scopes, grantTypes, clientRepository, configProperties);
        };
    }

    private Map<String, OAuth2Scope> initScopes(OAuth2ScopeRepository scopeRepository,
                                                ConfigProperties configProperties) {

        return configProperties.getAuthorizationServer()
                .getSupportedScopes()
                .stream()
                .map(name -> scopeRepository
                        .findByName(name)
                        .orElseGet(() -> scopeRepository.save(new OAuth2Scope(null, name))))
                .collect(Collectors.toMap(
                        OAuth2Scope::getName,
                        Function.identity()
                ));
    }

    private Map<String, OAuth2GrantType> initGrantTypes(OAuth2GrantTypeRepository grantTypeRepository,
                                                        ConfigProperties configProperties) {

        return configProperties.getAuthorizationServer()
                .getSupportedGrantTypes()
                .stream()
                .map(name -> grantTypeRepository
                        .findByName(name)
                        .orElseGet(() -> grantTypeRepository.save(new OAuth2GrantType(null, name))))
                .collect(Collectors.toMap(
                        OAuth2GrantType::getName,
                        Function.identity()
                ));
    }

    private void initAuthorizationServer(
            Map<String, OAuth2Scope> scopes,
            Map<String, OAuth2GrantType> grantTypes,
            AuthorizationServerRepository authorizationServerRepository,
            ConfigProperties configProperties) {

        var properties = configProperties.getAuthorizationServer();

        AuthorizationServer authorizationServer = authorizationServerRepository
                .findFirstByOrderByIdAsc()
                .orElseGet(AuthorizationServer::new);

        authorizationServer.setIssuerUrl(properties.getIssuerUrl());
        authorizationServer.setAuthorizationConsent(properties.isAuthorizationConsent());
        authorizationServer.setAccessTokenTtl(properties.getAccessTokenTtl());
        authorizationServer.setRefreshTokenTtl(properties.getRefreshTokenTtl());
        authorizationServer.setAuthorizationCodeTtl(properties.getAuthorizationCodeTtl());
        authorizationServer.setReuseRefreshTokens(properties.isReuseRefreshTokens());
        authorizationServer.setFreeLogin(properties.isFreeLogin());

        authorizationServer.setSupportedScopes(
                properties.getSupportedScopes().stream()
                        .map(scopes::get)
                        .collect(Collectors.toSet())
        );

        authorizationServer.setSupportedGrantTypes(
                properties.getSupportedGrantTypes().stream()
                        .map(grantTypes::get)
                        .collect(Collectors.toSet())
        );

        authorizationServerRepository.save(authorizationServer);
    }

    private void initUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ConfigProperties configProperties) {

        configProperties.getUsers().forEach(userProperties -> {

            User user = userRepository
                    .findByUsername(userProperties.getUsername())
                    .orElseGet(User::new);

            user.setUsername(userProperties.getUsername());
            user.setPassword(passwordEncoder.encode(userProperties.getPassword()));
            user.setFirstName(userProperties.getFirstName());
            user.setLastName(userProperties.getLastName());
            user.setEmail(userProperties.getEmail());
            user.setAddress(userProperties.getAddress());
            user.setPhoneNumber(userProperties.getPhoneNumber());
            user.setRoles(userProperties.getRoles());
            user.setGroups(userProperties.getGroups());
            user.setEnabled(true);
            user.setEmailVerified(true);

            userRepository.save(user);
        });
    }

    private void initClients(
            Map<String, OAuth2Scope> scopes,
            Map<String, OAuth2GrantType> grantTypes,
            OAuth2ClientRepository clientRepository,
            ConfigProperties configProperties) {

        for (Map.Entry<String, OAuth2ClientProperties> entry : configProperties.getOauth2Clients().entrySet()) {
            OAuth2ClientProperties clientProperties = entry.getValue();
            String clientName = entry.getKey();

            OAuth2Client client = clientRepository
                    .findByClientId(clientProperties.getClientId())
                    .orElseGet(OAuth2Client::new);

            client.setName(clientName);
            client.setClientId(clientProperties.getClientId());
            client.setClientSecret(clientProperties.getClientSecret());
            client.setClientUrl(clientProperties.getClientUrl());
            client.setRedirectUris(clientProperties.getRedirectUris());
            client.setPostLogoutRedirectUris(clientProperties.getPostLogoutRedirectUris());

            client.setScopes(clientProperties.getScopes().stream()
                    .map(name -> getScope(scopes, name, client.getClientId()))
                    .collect(Collectors.toSet()));

            client.setGrantTypes(clientProperties.getAuthorizationGrantTypes().stream()
                    .map(name -> getGrantType(grantTypes, name, client.getClientId()))
                    .collect(Collectors.toSet()));

            clientRepository.save(client);
        }
    }

    private OAuth2Scope getScope(Map<String, OAuth2Scope> scopes, String name, String clientId) {
        OAuth2Scope scope = scopes.get(name);
        if (scope == null) {
            throw new IllegalStateException("Scope OAuth2 [" + name + "] non valido per il client [" + clientId + "]");
        }

        return scope;
    }

    private OAuth2GrantType getGrantType(Map<String, OAuth2GrantType> grantTypes, String name, String clientId) {
        OAuth2GrantType grantType = grantTypes.get(name);
        if (grantType == null) {
            throw new IllegalStateException("Grant Type OAuth2 [" + name + "] non valido per il client [" + clientId + "]");
        }

        return grantType;
    }
}