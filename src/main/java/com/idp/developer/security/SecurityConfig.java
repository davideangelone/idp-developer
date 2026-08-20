package com.idp.developer.security;

import com.idp.developer.service.AuthorizationServerService;
import com.idp.developer.ui.LoginView;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(AuthorizationServerService service) {
        return new LoginPasswordEncoder(new PlainTextPasswordEncoder(), service);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain freeLoginSecurityFilterChain(HttpSecurity http, AuthorizationServerService service) {

        http.securityMatcher(new LoginRequestMatcher(service, true));
        enableH2ConsoleAccess(http);

        http.with(VaadinSecurityConfigurer.vaadin(), configurer ->
                configurer
                        .loginView(LoginView.class)
                        .anyRequest(AuthorizeHttpRequestsConfigurer.AuthorizedUrl::permitAll)
        );

        http.formLogin(form -> form
                .loginPage("/login")
                .permitAll()
        );

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain defaultLoginSecurityFilterChain(HttpSecurity http, AuthorizationServerService service) {

        http.securityMatcher(new LoginRequestMatcher(service, false));
        enableH2ConsoleAccess(http);

        http.with(VaadinSecurityConfigurer.vaadin(), configurer ->
                configurer
                        .anyRequest(AuthorizeHttpRequestsConfigurer.AuthorizedUrl::authenticated)
        );

        http.formLogin(Customizer.withDefaults());

        return http.build();
    }

    private void enableH2ConsoleAccess(HttpSecurity http) {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**")) //NOSONAR
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                )
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
    }
}
