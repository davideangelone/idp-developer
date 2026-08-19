package com.idp.developer.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.idp.developer.entity.AuthorizationServer;
import com.idp.developer.entity.OAuth2AuthenticationMethod;
import com.idp.developer.entity.OAuth2Claim;
import com.idp.developer.entity.OAuth2Client;
import com.idp.developer.entity.OAuth2GrantType;
import com.idp.developer.entity.OAuth2Scope;
import com.idp.developer.entity.User;
import com.idp.developer.properties.ConfigProperties;
import com.idp.developer.properties.OAuth2ClientProperties;
import com.idp.developer.repository.AuthorizationServerRepository;
import com.idp.developer.repository.OAuth2AuthenticationMethodRepository;
import com.idp.developer.repository.OAuth2ClaimRepository;
import com.idp.developer.repository.OAuth2ClientRepository;
import com.idp.developer.repository.OAuth2GrantTypeRepository;
import com.idp.developer.repository.OAuth2ScopeRepository;
import com.idp.developer.repository.UserRepository;
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
            PasswordEncoder passwordEncoder,
            ConfigProperties configProperties) {

        return args -> {
            Map<String, OAuth2AuthenticationMethod> authenticationMethods = initAuthenticationMethods(authenticationMethodRepository, configProperties);
            Map<String, OAuth2Scope> scopes = initScopes(scopeRepository, configProperties);
            Map<String, OAuth2GrantType> grantTypes = initGrantTypes(grantTypeRepository, configProperties);

            initAlwaysClaims(claimRepository, configProperties);
            initScopeClaims(scopeRepository, claimRepository, configProperties);
            initAuthorizationServer(authenticationMethods, scopes, grantTypes, authorizationServerRepository, configProperties);
            initUsers(userRepository, passwordEncoder, configProperties);
            initClients(authenticationMethods, scopes, grantTypes, clientRepository, configProperties);
        };
    }

    private Map<String, OAuth2AuthenticationMethod> initAuthenticationMethods(OAuth2AuthenticationMethodRepository authenticationMethodRepository,
                                                                              ConfigProperties configProperties) {

        Map<String, OAuth2AuthenticationMethod> authenticationMethods = new HashMap<>();

        for (String name : configProperties.getAuthorizationServer().getSupportedAuthenticationMethods()) {

            OAuth2AuthenticationMethod authenticationMethod = authenticationMethodRepository.findByName(name);
            if (null == authenticationMethod) {
                authenticationMethod = new OAuth2AuthenticationMethod();
                authenticationMethod.setName(name);
                authenticationMethod = authenticationMethodRepository.save(authenticationMethod);
            }

            authenticationMethods.put(authenticationMethod.getName(), authenticationMethod);
        }

        return authenticationMethods;
    }

    private Map<String, OAuth2Scope> initScopes(OAuth2ScopeRepository scopeRepository,
                                                ConfigProperties configProperties) {

        Map<String, OAuth2Scope> scopes = new HashMap<>();

        for (String name : configProperties.getAuthorizationServer().getSupportedScopes()) {

            OAuth2Scope scope = scopeRepository.findByName(name);
            if (null == scope) {
                scope = new OAuth2Scope();
                scope.setName(name);
                scope = scopeRepository.save(scope);
            }

            scopes.put(scope.getName(), scope);
        }

        return scopes;
    }

    private void initAlwaysClaims(
            OAuth2ClaimRepository claimRepository,
            ConfigProperties configProperties) {

        for (Map.Entry<String, String> entry : configProperties.getClaims().getAlways().entrySet()) {

            OAuth2Claim claim = claimRepository
                    .findByScopeIsNullAndName(entry.getKey())
                    .orElseGet(OAuth2Claim::new);

            claim.setScope(null);
            claim.setAlways(true);
            claim.setName(entry.getKey());
            claim.setUserProperty(entry.getValue());

            claimRepository.save(claim);
        }
    }

    private void initScopeClaims(OAuth2ScopeRepository scopeRepository,
                                 OAuth2ClaimRepository claimRepository,
                                 ConfigProperties configProperties) {

        for (Map.Entry<String, Map<String, String>> scopeClaims : configProperties.getClaims().getScopes().entrySet()) {

            OAuth2Scope scope = scopeRepository.findByName(scopeClaims.getKey());
            if (scope == null) {
                continue;
            }

            for (Map.Entry<String, String> claimEntry : scopeClaims.getValue().entrySet()) {

                OAuth2Claim claim = claimRepository
                        .findByScopeAndName(scope, claimEntry.getKey())
                        .orElseGet(OAuth2Claim::new);

                claim.setScope(scope);
                claim.setAlways(false);
                claim.setName(claimEntry.getKey());
                claim.setUserProperty(claimEntry.getValue());

                claimRepository.save(claim);
            }
        }
    }

    private Map<String, OAuth2GrantType> initGrantTypes(OAuth2GrantTypeRepository grantTypeRepository,
                                                        ConfigProperties configProperties) {

        Map<String, OAuth2GrantType> grantTypes = new HashMap<>();

        for (String name : configProperties.getAuthorizationServer().getSupportedGrantTypes()) {
            OAuth2GrantType grantType = grantTypeRepository.findByName(name);
            if (null == grantType) {
                grantType = new OAuth2GrantType();
                grantType.setName(name);
                grantType = grantTypeRepository.save(grantType);
            }
            grantTypes.put(grantType.getName(), grantType);
        }

        return grantTypes;
    }

    private void initAuthorizationServer(
            Map<String, OAuth2AuthenticationMethod> authenticationMethods,
            Map<String, OAuth2Scope> scopes,
            Map<String, OAuth2GrantType> grantTypes,
            AuthorizationServerRepository authorizationServerRepository,
            ConfigProperties configProperties) {

        var properties = configProperties.getAuthorizationServer();

        AuthorizationServer authorizationServer = authorizationServerRepository
                .findFirstByOrderByIdAsc()
                .orElseGet(AuthorizationServer::new);

        authorizationServer.setIssuerUrl(properties.getIssuerUrl());
        authorizationServer.setFreeLogin(properties.isFreeLogin());

        authorizationServer.setSupportedAuthenticationMethods(
                properties.getSupportedAuthenticationMethods().stream()
                        .map(authenticationMethods::get)
                        .collect(Collectors.toSet())
        );

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
            Map<String, OAuth2AuthenticationMethod> authenticationMethods,
            Map<String, OAuth2Scope> scopes,
            Map<String, OAuth2GrantType> grantTypes,
            OAuth2ClientRepository clientRepository,
            ConfigProperties configProperties) {

        for (OAuth2ClientProperties clientProperties : configProperties.getOauth2Clients()) {
            OAuth2Client client = clientRepository
                    .findByClientId(clientProperties.getClientId())
                    .orElseGet(OAuth2Client::new);

            client.setDescription(clientProperties.getDescription());
            client.setClientId(clientProperties.getClientId());
            client.setClientSecret(clientProperties.getClientSecret());

            client.setClientAuthenticationMethods(clientProperties.getClientAuthenticationMethods().stream()
                    .map(name -> getClientAuthenticationMethod(authenticationMethods, name, client.getClientId()))
                    .collect(Collectors.toSet()));

            client.setClientUrl(clientProperties.getClientUrl());
            client.setRedirectUris(clientProperties.getRedirectUris());
            client.setPostLogoutRedirectUris(clientProperties.getPostLogoutRedirectUris());

            client.setScopes(clientProperties.getScopes().stream()
                    .map(name -> getScope(scopes, name, client.getClientId()))
                    .collect(Collectors.toSet()));

            client.setAuthorizationGrantTypes(clientProperties.getAuthorizationGrantTypes().stream()
                    .map(name -> getAuthorizationGrantType(grantTypes, name, client.getClientId()))
                    .collect(Collectors.toSet()));

            client.setAuthorizationConsent(clientProperties.isAuthorizationConsent());
            client.setRequireProofKey(clientProperties.isRequireProofKey());
            client.setAccessTokenTtl(clientProperties.getAccessTokenTtl());
            client.setRefreshTokenTtl(clientProperties.getRefreshTokenTtl());
            client.setAuthorizationCodeTtl(clientProperties.getAuthorizationCodeTtl());
            client.setReuseRefreshTokens(clientProperties.isReuseRefreshTokens());

            List<String> scopesNames = client.getScopes().stream()
                                                         .map(OAuth2Scope::getName)
                                                         .toList();
            log.info("Inizializzazione client '{}' scopes: {}", client.getClientId(), scopesNames);
            clientRepository.save(client);
        }
    }

    private OAuth2AuthenticationMethod getClientAuthenticationMethod(Map<String, OAuth2AuthenticationMethod> authenticationMethods, String name, String clientId) {
        OAuth2AuthenticationMethod authenticationMethod = authenticationMethods.get(name);
        if (authenticationMethod == null) {
            throw new IllegalStateException("Authentication Method OAuth2 [" + name + "] non valido per il client [" + clientId + "]");
        }

        return authenticationMethod;
    }

    private OAuth2Scope getScope(Map<String, OAuth2Scope> scopes, String name, String clientId) {
        OAuth2Scope scope = scopes.get(name);
        if (scope == null) {
            throw new IllegalStateException("Scope OAuth2 [" + name + "] non valido per il client [" + clientId + "]");
        }

        return scope;
    }

    private OAuth2GrantType getAuthorizationGrantType(Map<String, OAuth2GrantType> grantTypes, String name, String clientId) {
        OAuth2GrantType grantType = grantTypes.get(name);
        if (grantType == null) {
            throw new IllegalStateException("Authorization Grant Type OAuth2 [" + name + "] non valido per il client [" + clientId + "]");
        }

        return grantType;
    }
}