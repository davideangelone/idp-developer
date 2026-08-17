package com.idp.developer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DeveloperIdpApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeveloperIdpApplication.class, args);
    }
}
