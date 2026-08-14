package com.idp.enterpriseidp.security;

import com.idp.enterpriseidp.properties.ConfigProperties;
import com.idp.enterpriseidp.service.CustomUserDetailsService;
import com.idp.enterpriseidp.ui.LoginView;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder(ConfigProperties configProperties) {
        if (configProperties.getAuthorizationServer().isCustomLoginPage()) {
            return AlwaysMatchesPasswordEncoder.getInstance();
        }
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(ConfigProperties configProperties) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder(configProperties));
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http, ConfigProperties configProperties) {

        if (configProperties.getAuthorizationServer().isCustomLoginPage()) {
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

        http.authenticationProvider(daoAuthenticationProvider(configProperties));

        return http.build();
    }
}
