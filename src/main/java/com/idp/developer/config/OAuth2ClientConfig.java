package com.idp.developer.config;

import java.util.UUID;

import com.idp.developer.properties.ConfigProperties;
import com.idp.developer.properties.OAuth2ClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

@Configuration
@Slf4j
public class OAuth2ClientConfig {

    @Bean
    public RegisteredClientRepository registeredClientRepository(ConfigProperties configProperties) {
        var clients = configProperties.getOauth2Clients()
                .values()
                .stream()
                .map(client -> createRegisteredClient(configProperties, client))
                .toList();
        return new InMemoryRegisteredClientRepository(clients);
    }

    private RegisteredClient createRegisteredClient(ConfigProperties configProperties, OAuth2ClientProperties oauth2ClientProperties) {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(oauth2ClientProperties.getClientId())
                .clientSecret(oauth2ClientProperties.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantTypes(grantTypes ->
                        grantTypes.addAll(oauth2ClientProperties.getAuthorizationGrantTypes().stream()
                                .map(AuthorizationGrantType::new)
                                .toList())
                )
                .redirectUris(redirectUris ->
                        redirectUris.addAll(oauth2ClientProperties.getRedirectUris()))
                .postLogoutRedirectUris(postLogoutRedirectUris ->
                        postLogoutRedirectUris.addAll(oauth2ClientProperties.getPostLogoutRedirectUris()))
                .scopes(scopes -> scopes.addAll(oauth2ClientProperties.getScopes()))
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(configProperties.getAuthorizationServer().isAuthorizationConsent())
                        .requireProofKey(true)
                        .build())
                .tokenSettings(getTokenSettings(configProperties))
                .build();

        log.info("Registered client '{}' scopes: {}", registeredClient.getClientId(), registeredClient.getScopes());
        return registeredClient;
    }

    private TokenSettings getTokenSettings(ConfigProperties configProperties) {
        return TokenSettings.builder()
                .accessTokenTimeToLive(configProperties.getAuthorizationServer().getAccessTokenTtl())
                .refreshTokenTimeToLive(configProperties.getAuthorizationServer().getRefreshTokenTtl())
                .authorizationCodeTimeToLive(configProperties.getAuthorizationServer().getAuthorizationCodeTtl())
                .reuseRefreshTokens(configProperties.getAuthorizationServer().isReuseRefreshTokens())
                .build();
    }

}
