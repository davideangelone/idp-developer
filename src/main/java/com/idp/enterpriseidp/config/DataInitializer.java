package com.idp.enterpriseidp.config;

import com.idp.enterpriseidp.domain.User;
import com.idp.enterpriseidp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Value("${app.username1}")
    private String username1;

    @Value("${app.password1}")
    private String password1;

    @Value("${app.username2}")
    private String username2;

    @Value("${app.password2}")
    private String password2;

    @Bean
    CommandLineRunner initUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (!userRepository.existsByUsername(username1)) {
                User user = new User();
                user.setUsername(username1);
                user.setEmail(username1 + "@example.com");
                user.setPassword(passwordEncoder.encode(password1));
                user.setFirstName("Mario");
                user.setLastName("Rossi");
                user.setAddress("Via Roma 1, Bologna");
                user.setPhoneNumber("+39 333 1234567");
                user.setEnabled(true);
                user.setEmailVerified(true);

                userRepository.save(user);
            }

            if (!userRepository.existsByUsername(username2)) {
                User user = new User();
                user.setUsername(username2);
                user.setEmail(username2 + "@example.com");
                user.setPassword(passwordEncoder.encode(password2));
                user.setFirstName("John");
                user.setLastName("Doe");
                user.setAddress("123 Main Street");
                user.setPhoneNumber("+1 555 1234567");
                user.setEnabled(true);
                user.setEmailVerified(true);

                userRepository.save(user);
            }
        };
    }
}