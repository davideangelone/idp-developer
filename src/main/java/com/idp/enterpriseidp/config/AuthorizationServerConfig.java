package com.idp.enterpriseidp.config;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import com.idp.enterpriseidp.properties.ConfigProperties;
import com.idp.enterpriseidp.properties.JwtProperties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationServerConfig.class);

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

        logger.info("Registered client '{}' scopes: {}", registeredClient.getClientId(), registeredClient.getScopes());
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

    @Bean
    public JWKSource<SecurityContext> jwkSource(ConfigProperties configProperties) {

        JwtProperties jwtProperties = configProperties.getJwt();

        try {
            KeyStore keyStore = KeyStore.getInstance(jwtProperties.getKeyStoreType());

            try (InputStream inputStream = jwtProperties.getKeyStore().getInputStream()) {
                keyStore.load(inputStream, jwtProperties.getKeyStorePassword().toCharArray());
            }

            RSAPrivateKey privateKey = (RSAPrivateKey) keyStore.getKey(jwtProperties.getKeyAlias(), jwtProperties.getKeyPassword().toCharArray());
            Certificate certificate = keyStore.getCertificate(jwtProperties.getKeyAlias());
            RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();

            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(jwtProperties.getKeyAlias())
                    .build();

            return new ImmutableJWKSet<>(new JWKSet(rsaKey));
        } catch (Exception e) {
            throw new IllegalStateException("Impossibile inizializzare la chiave per la firma dei token JWT", e);
        }
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(ConfigProperties configProperties) {
        return AuthorizationServerSettings.builder()
                .issuer(configProperties.getAuthorizationServer().getIssuerUrl())
                .build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http, ConfigProperties configProperties) {
        http
                .oauth2AuthorizationServer(authorizationServer -> {
                    http.securityMatcher(authorizationServer.getEndpointsMatcher());
                    authorizationServer
                            .oidc(oidc -> oidc
                                    .providerConfigurationEndpoint(providerConfiguration ->
                                            providerConfiguration.providerConfigurationCustomizer(builder ->
                                                    builder.scopes(scopes -> {
                                                                scopes.clear();
                                                                scopes.addAll(configProperties.getAuthorizationServer().getSupportedScopes());
                                                            }
                                                    ))
                                    )
                            );
                })
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );

        return http.build();
    }
}
