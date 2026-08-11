package com.idp.enterpriseidp.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(StartupLogger.class);

    private final Environment environment;

    public StartupLogger(Environment environment) {
        this.environment = environment;
    }

    @Value("${server.port:8080}")
    private String port;

    @Value("${server.address:localhost}")
    private String address;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String baseUrl = String.format("http://%s:%s", address, port);
        logger.info("====================================================================");
        logger.info("Applicazione avviata con successo!");
        logger.info("Application URL: {}", baseUrl);
        logger.info("Current profile: {}", List.of(environment.getActiveProfiles()));
        logger.info("Endpoint: {}/.well-known/openid-configuration", baseUrl);
        logger.info("====================================================================");
    }
}
