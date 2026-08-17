package com.idp.developer.config;

import com.idp.developer.properties.ConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
public class AuthorizationServerConfig {

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
