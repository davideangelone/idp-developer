package com.idp.developer.config;

import java.util.UUID;

import com.idp.developer.properties.ConfigProperties;
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
public class RegisteredClientConfig {

    @Bean
    public RegisteredClientRepository registeredClientRepository(ConfigProperties configProperties) {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(configProperties.getOauth2Client().getClientId())
                .clientSecret("{noop}" + configProperties.getOauth2Client().getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantTypes(grantTypes ->
                        grantTypes.addAll(configProperties.getOauth2Client().getAuthorizationGrantTypes().stream()
                                .map(AuthorizationGrantType::new)
                                .toList())
                )
                .redirectUris(redirectUris ->
                        redirectUris.addAll(configProperties.getOauth2Client().getRedirectUris()))
                .postLogoutRedirectUris(postLogoutRedirectUris ->
                        postLogoutRedirectUris.addAll(configProperties.getOauth2Client().getPostLogoutRedirectUris()))
                .scopes(scopes -> scopes.addAll(configProperties.getOauth2Client().getScopes()))
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(configProperties.getAuthorizationServer().isAuthorizationConsent())
                        .requireProofKey(true)
                        .build())
                .tokenSettings(getTokenSettings(configProperties))
                .build();

        log.info("Registered client '{}' scopes: {}", registeredClient.getClientId(), registeredClient.getScopes());
        return new InMemoryRegisteredClientRepository(registeredClient);
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
