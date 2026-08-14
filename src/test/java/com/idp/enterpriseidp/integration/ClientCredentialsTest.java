package com.idp.enterpriseidp.integration;

import java.util.Arrays;
import java.util.Map;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClientCredentialsTest extends AbstractIdpIntegrationMockMvcTest {

    @Test
    @DisplayName("Client Credentials Flow restituisce access_token senza claim utente e sub == client_id")
    void clientCredentials_returnsAccessToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(configProperties.getOauth2Client().getClientId(), configProperties.getOauth2Client().getClientSecret()))
                        .param("grant_type", "client_credentials")
                        .param("scope", "openid profile email address phone")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> tokens = parseJson(result.getResponse().getContentAsString());
        assertThat(tokens)
                .containsKey("access_token")
                .containsEntry("token_type", "Bearer");
        assertThat(tokens.get("expires_in")).isNotNull();

        String scope = (String) tokens.get("scope");
        assertThat(scope).isNotNull();
        assertThat(Arrays.asList(scope.split(" "))).containsExactlyInAnyOrder("openid", "profile", "email", "address", "phone");

        String accessToken = (String) tokens.get("access_token");
        SignedJWT signedJWT = SignedJWT.parse(accessToken);
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        assertThat(claims.getSubject()).isEqualTo(configProperties.getOauth2Client().getClientId());
        assertThat(claims.getClaim("email")).isNull();
        assertThat(claims.getClaim("preferred_username")).isNull();
        assertThat(claims.getClaim("name")).isNull();
    }
}
