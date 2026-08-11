package com.idp.enterpriseidp.config;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.idp.enterpriseidp.domain.User;
import com.idp.enterpriseidp.service.CustomUserDetailsService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
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
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
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

    @Value("${app.issuer-url}")
    private String issuerUrl;

    @Value("${app.authorizationConsent}")
    private boolean authorizationConsent;

    @Value("${oauth2.clientId}")
    private String clientId;

    @Value("${oauth2.clientSecret}")
    private String clientSecret;

    @Value("${oauth2.redirectUrlClient}")
    private String redirectUrlClient;

    @Value("${oauth2.redirectUrlTest}")
    private String redirectUrlTest;

    @Value("${oauth2.postLogoutRedirectUrl}")
    private String postLogoutRedirectUrl;

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret("{noop}" + clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantTypes(grantTypes ->
                        grantTypes.addAll(List.of(
                                AuthorizationGrantType.CLIENT_CREDENTIALS,
                                AuthorizationGrantType.AUTHORIZATION_CODE,
                                AuthorizationGrantType.REFRESH_TOKEN
                        ))
                )
                .redirectUri(redirectUrlClient)
                .redirectUri(redirectUrlTest)
                .postLogoutRedirectUri(postLogoutRedirectUrl)
                .scopes(scopes -> scopes.addAll(DEFAULT_SCOPES))
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(authorizationConsent)
                        .requireProofKey(true)
                        .build())
                .tokenSettings(getTokenSettings())
                .build();

        logger.info("Registered client '{}' scopes: {}", registeredClient.getClientId(), registeredClient.getScopes());
        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    private TokenSettings getTokenSettings() {
        return TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(5))
                .refreshTokenTimeToLive(Duration.ofDays(30))
                .authorizationCodeTimeToLive(Duration.ofMinutes(5))
                .reuseRefreshTokens(false)
                .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(
            @Value("${app.jwt.key-store}") Resource keyStoreResource,
            @Value("${app.jwt.key-store-password}") String keyStorePassword,
            @Value("${app.jwt.key-alias}") String keyAlias,
            @Value("${app.jwt.key-password}") String keyPassword) throws Exception {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        try (InputStream inputStream = keyStoreResource.getInputStream()) {
            keyStore.load(inputStream, keyStorePassword.toCharArray());
        }

        RSAPrivateKey privateKey = (RSAPrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray());
        Certificate certificate = keyStore.getCertificate(keyAlias);
        RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(keyAlias)
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
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(issuerUrl)
                .build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) {
        http
                .oauth2AuthorizationServer((authorizationServer) -> {
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
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .anyRequest().authenticated()
                )
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(org.springframework.http.MediaType.TEXT_HTML)
                        )
                );

        return http.build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            var principal = context.getPrincipal();
            if (principal == null || principal.getPrincipal() == null) {
                return;
            }

            Object userDetails = principal.getPrincipal();
            if (userDetails instanceof CustomUserDetailsService.CustomUserDetails(User user)) {
                context.getClaims().claim("sub", user.getId());
                context.getClaims().claim("name", user.getFirstName() + " " + user.getLastName());
                context.getClaims().claim("given_name", user.getFirstName());
                context.getClaims().claim("family_name", user.getLastName());
                context.getClaims().claim("email", user.getEmail());
                context.getClaims().claim("email_verified", user.isEmailVerified());
                context.getClaims().claim("address", user.getAddress());
                context.getClaims().claim("phone_number", user.getPhoneNumber());
                context.getClaims().claim("preferred_username", user.getUsername());
            }

            if (null != principal.getName()) {
                logger.info(
                        "Generating token: user={}, tokenType={}, clientId={}, grantType={}, scopes={}, claims={}",
                        principal.getName(),
                        context.getTokenType().getValue(),
                        context.getRegisteredClient().getClientId(),
                        Optional.ofNullable(context.getAuthorizationGrantType()).map(AuthorizationGrantType::getValue).orElse(null),
                        context.getAuthorizedScopes(),
                        context.getClaims().build().getClaims()
                );
            }
        };
    }
}
