package com.idp.enterpriseidp.config;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import com.idp.enterpriseidp.properties.ConfigProperties;
import com.idp.enterpriseidp.properties.JwtProperties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@Configuration
public class JwtConfig {

    @Bean
    public JWKSource<SecurityContext> jwkSource(ConfigProperties configProperties) {

        JwtProperties jwtProperties = configProperties.getJwt();

        try {
            KeyStore keyStore = KeyStore.getInstance(jwtProperties.getKeyStoreType());

            try (InputStream inputStream = jwtProperties.getKeyStore().getInputStream()) {
                keyStore.load(inputStream, jwtProperties.getKeyStorePassword().toCharArray());
            }

            RSAPrivateKey privateKey = (RSAPrivateKey) keyStore.getKey(jwtProperties.getKeyAlias(), jwtProperties.getKeyPassword().toCharArray());
            Certificate certificate = keyStore.getCertificate(jwtProperties.getKeyAlias());
            RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();

            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(jwtProperties.getKeyAlias())
                    .build();

            return new ImmutableJWKSet<>(new JWKSet(rsaKey));
        } catch (Exception e) {
            throw new IllegalStateException("Impossibile inizializzare la chiave per la firma dei token JWT", e);
        }
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

}
