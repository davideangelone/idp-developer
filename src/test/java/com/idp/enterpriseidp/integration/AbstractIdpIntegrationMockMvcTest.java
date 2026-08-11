package com.idp.enterpriseidp.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIdpIntegrationMockMvcTest {

    @Autowired
    protected MockMvc mockMvc;

    @Value("${app.issuer-url}")
    protected String issuerUrl;

    @Value("${app.authorizationConsent}")
    protected boolean authorizationConsent;

    @Value("${oauth2.clientId}")
    protected String clientId;

    @Value("${oauth2.clientSecret}")
    protected String clientSecret;

    @Value("${oauth2.redirectUrlClient}")
    protected String redirectUrlClient;

    @Value("${oauth2.postLogoutRedirectUrl}")
    protected String postLogoutRedirectUrl;

    @Value("${app.username1}")
    protected String username1;

    @Value("${app.password1}")
    protected String password1;

    @Value("${app.username2}")
    protected String username2;

    @Value("${app.password2}")
    protected String password2;

    protected String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    protected String generateCodeChallenge(String codeVerifier) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    protected String extractCodeFromLocation(String location) {
        if (location == null) {
            return null;
        }
        org.springframework.web.util.UriComponentsBuilder builder =
                org.springframework.web.util.UriComponentsBuilder.fromUriString(location);
        return builder.build().getQueryParams().getFirst("code");
    }
}
