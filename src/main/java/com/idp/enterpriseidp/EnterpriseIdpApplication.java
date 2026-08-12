package com.idp.enterpriseidp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EnterpriseIdpApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnterpriseIdpApplication.class, args);
    }
}
