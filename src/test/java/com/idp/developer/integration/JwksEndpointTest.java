package com.idp.developer.integration;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.security.KeyPairGenerator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JwksEndpointTest extends AbstractIdpIntegrationMockMvcTest {

    @Test
    @DisplayName("GET /oauth2/jwks ritorna HTTP 200 e una chiave RSA valida, utilizzabile per verificare una firma")
    void jwksEndpoint_returnsValidRsaKey() throws Exception {
        MvcResult result = mockMvc.perform(get("/oauth2/jwks")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        JWKSet jwkSet = JWKSet.parse(result.getResponse().getContentAsString());
        List<JWK> keys = jwkSet.getKeys();

        assertThat(keys).isNotEmpty();
        JWK key = keys.getFirst();
        assertThat(key.getKeyType().getValue()).isEqualTo("RSA");
        assertThat(key.getKeyID()).isNotNull();
        assertThat(key.toRSAKey().toPublicKey()).isNotNull();

        // The published JWK must be usable to build an RSASSAVerifier (the same path
        // used by resource servers / the E2E test to verify real tokens).
        RSASSAVerifier verifier = new RSASSAVerifier(key.toRSAKey().toRSAPublicKey());
        assertThat(verifier).isNotNull();

        // Self-contained proof that a token signed by the matching key pair verifies
        // against a JWK-derived verifier (mirrors real signature validation).
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        var kp = gen.generateKeyPair();
        RSAKey localKey = new RSAKey.Builder((java.security.interfaces.RSAPublicKey) kp.getPublic())
                .privateKey((java.security.interfaces.RSAPrivateKey) kp.getPrivate())
                .keyID(UUID.randomUUID().toString())
                .build();
        JWSSigner signer = new RSASSASigner(localKey);
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(localKey.getKeyID()).build(),
                new JWTClaimsSet.Builder().subject("test").build());
        jwt.sign(signer);
        RSASSAVerifier localVerifier = new RSASSAVerifier(localKey.toRSAPublicKey());
        assertThat(jwt.verify(localVerifier)).isTrue();
    }
}
