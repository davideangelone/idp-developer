package com.idp.enterpriseidp.config;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.idp.enterpriseidp.properties.AppProperties;
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
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

    private static final Set<String> DEFAULT_SCOPES = Set.of(
            OidcScopes.OPENID,
            OidcScopes.PROFILE,
            OidcScopes.EMAIL,
            OidcScopes.ADDRESS,
            OidcScopes.PHONE
    );

    @Bean
    public RegisteredClientRepository registeredClientRepository(AppProperties appProperties) {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(appProperties.getOauth2().getClientId())
                .clientSecret("{noop}" + appProperties.getOauth2().getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantTypes(grantTypes ->
                        grantTypes.addAll(List.of(
                                AuthorizationGrantType.CLIENT_CREDENTIALS,
                                AuthorizationGrantType.AUTHORIZATION_CODE,
                                AuthorizationGrantType.REFRESH_TOKEN
                        ))
                )
                .redirectUri(appProperties.getOauth2().getRedirectUrlClient())
                .redirectUri(appProperties.getOauth2().getRedirectUrlTest())
                .postLogoutRedirectUri(appProperties.getOauth2().getPostLogoutRedirectUrl())
                .scopes(scopes -> scopes.addAll(DEFAULT_SCOPES))
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(appProperties.getAuthorizationServer().isAuthorizationConsent())
                        .requireProofKey(true)
                        .build())
                .tokenSettings(getTokenSettings(appProperties))
                .build();

        logger.info("Registered client '{}' scopes: {}", registeredClient.getClientId(), registeredClient.getScopes());
        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    private TokenSettings getTokenSettings(AppProperties appProperties) {
        return TokenSettings.builder()
                .accessTokenTimeToLive(appProperties.getAuthorizationServer().getAccessTokenTtl())
                .refreshTokenTimeToLive(appProperties.getAuthorizationServer().getRefreshTokenTtl())
                .authorizationCodeTimeToLive(appProperties.getAuthorizationServer().getAuthorizationCodeTtl())
                .reuseRefreshTokens(appProperties.getAuthorizationServer().isReuseRefreshTokens())
                .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(AppProperties appProperties) throws Exception {

        JwtProperties jwtProperties = appProperties.getJwt();
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

        return new ImmutableJWKSet<>(
                new JWKSet(rsaKey)
        );
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(AppProperties appProperties) {
        return AuthorizationServerSettings.builder()
                .issuer(appProperties.getAuthorizationServer().getIssuerUrl())
                .build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) {
        http
                .oauth2AuthorizationServer(authorizationServer -> {
                    http.securityMatcher(authorizationServer.getEndpointsMatcher());
                    authorizationServer
                            .oidc(oidc -> oidc
                                    .providerConfigurationEndpoint(providerConfiguration ->
                                            providerConfiguration.providerConfigurationCustomizer(builder ->
                                                    builder.scopes(scopes -> {
                                                                scopes.clear();
                                                                scopes.addAll(DEFAULT_SCOPES);
                                                            }
                                                    ))
                                    )
                            );
                })
                .authorizeHttpRequests(authorize ->
                        authorize
                                .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(org.springframework.http.MediaType.TEXT_HTML)
                        )
                );

        return http.build();
    }
}
