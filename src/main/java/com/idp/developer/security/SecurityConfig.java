package com.idp.developer.security;

import com.idp.developer.properties.ConfigProperties;
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
    public PasswordEncoder passwordEncoder(ConfigProperties configProperties) {
        PasswordEncoder encoder = new PlainTextPasswordEncoder();
        if (configProperties.getAuthorizationServer().isFreeLogin()) {
            encoder = new FreeLoginPasswordEncoder(encoder);
        }
        return encoder;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http, ConfigProperties configProperties) {

        boolean freeLogin = configProperties.getAuthorizationServer().isFreeLogin();

        enableH2ConsoleAccess(http);

        if (freeLogin) {
            http.with(VaadinSecurityConfigurer.vaadin(), configurer ->
                    configurer
                            .loginView(LoginView.class)
                            .anyRequest(AuthorizeHttpRequestsConfigurer.AuthorizedUrl::permitAll)
            ).formLogin(form -> form
                    .loginPage("/login")
                    .permitAll()
            );
        } else {
            http.with(VaadinSecurityConfigurer.vaadin(), configurer ->
                    configurer.anyRequest(AuthorizeHttpRequestsConfigurer.AuthorizedUrl::authenticated)
            ).formLogin(Customizer.withDefaults());
        }

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
