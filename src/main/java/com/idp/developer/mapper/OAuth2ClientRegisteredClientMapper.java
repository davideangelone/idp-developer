package com.idp.developer.mapper;

import java.util.Set;
import java.util.function.Consumer;

import com.idp.developer.entity.OAuth2AuthenticationMethod;
import com.idp.developer.entity.OAuth2Client;
import com.idp.developer.entity.OAuth2GrantType;
import com.idp.developer.entity.OAuth2Scope;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OAuth2ClientRegisteredClientMapper {

    public RegisteredClient toRegisteredClient(OAuth2Client client) {

        RegisteredClient registeredClient = RegisteredClient.withId(String.valueOf(client.getId()))
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .clientAuthenticationMethods(getClientAuthenticationMethods(client))
                .authorizationGrantTypes(getAuthorizationGrantTypes(client))
                .redirectUris(uris -> uris.addAll(client.getRedirectUris()))
                .postLogoutRedirectUris(uris -> uris.addAll(client.getPostLogoutRedirectUris()))
                .scopes(getScopes(client))
                .clientSettings(getClientSettings(client))
                .tokenSettings(getTokenSettings(client))
                .build();

        log.info("Recupero client '{}' scopes: {}", registeredClient.getClientId(), registeredClient.getScopes());
        return registeredClient;
    }

    private @NonNull TokenSettings getTokenSettings(OAuth2Client client) {
        return TokenSettings.builder()
                .accessTokenTimeToLive(client.getAccessTokenTtl())
                .refreshTokenTimeToLive(client.getRefreshTokenTtl())
                .authorizationCodeTimeToLive(client.getAuthorizationCodeTtl())
                .reuseRefreshTokens(client.isReuseRefreshTokens())
                .build();
    }

    private @NonNull ClientSettings getClientSettings(OAuth2Client client) {
        return ClientSettings.builder()
                .requireAuthorizationConsent(client.isAuthorizationConsent())
                .requireProofKey(client.isRequireProofKey())
                .build();
    }

    private @NonNull Consumer<Set<String>> getScopes(OAuth2Client client) {
        return scopes ->
                scopes.addAll(client.getScopes().stream()
                        .map(OAuth2Scope::getName)
                        .toList());
    }

    private @NonNull Consumer<Set<AuthorizationGrantType>> getAuthorizationGrantTypes(OAuth2Client client) {
        return authorizationGrantTypes ->
                authorizationGrantTypes.addAll(
                        client.getAuthorizationGrantTypes()
                                .stream()
                                .map(OAuth2GrantType::getName)
                                .map(AuthorizationGrantType::new)
                                .toList()
                );
    }

    private @NonNull Consumer<Set<ClientAuthenticationMethod>> getClientAuthenticationMethods(OAuth2Client client) {
        return clientAuthenticationMethods ->
                clientAuthenticationMethods.addAll(
                        client.getClientAuthenticationMethods().stream()
                                .map(OAuth2AuthenticationMethod::getName)
                                .map(ClientAuthenticationMethod::new)
                                .toList()
                );
    }
}
