package com.idp.developer.config;

import com.idp.developer.entity.User;
import com.idp.developer.properties.ConfigProperties;
import com.idp.developer.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                ConfigProperties configProperties) {

        return args -> configProperties.getUsers().forEach(userProperties -> {

            User user = userRepository.findByUsername(userProperties.getUsername()).orElseGet(User::new);
            user.setUsername(userProperties.getUsername());
            user.setPassword(passwordEncoder.encode(userProperties.getPassword()));
            user.setFirstName(userProperties.getFirstName());
            user.setLastName(userProperties.getLastName());
            user.setEmail(userProperties.getEmail());
            user.setAddress(userProperties.getAddress());
            user.setPhoneNumber(userProperties.getPhoneNumber());
            user.setRoles(userProperties.getRoles());
            user.setGroups(userProperties.getGroups());
            user.setEnabled(true);
            user.setEmailVerified(true);

            userRepository.save(user);
        });
    }
}