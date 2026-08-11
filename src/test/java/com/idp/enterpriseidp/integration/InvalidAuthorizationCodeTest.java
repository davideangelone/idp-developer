package com.idp.enterpriseidp.integration;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InvalidAuthorizationCodeTest extends AbstractIdpIntegrationMockMvcTest {

    @Test
    @DisplayName("Authorization code inesistente restituisce invalid_grant")
    void nonExistentCode_returnsInvalidGrant() throws Exception {
        MvcResult result = mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(clientId, clientSecret))
                        .param("grant_type", "authorization_code")
                        .param("code", "non-existent-code")
                        .param("redirect_uri", redirectUrlClient)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andReturn();

        Map<String, Object> error = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
        assertThat(error.get("error")).isEqualTo("invalid_grant");
    }

    @Test
    @DisplayName("Code verifier errato restituisce invalid_grant")
    void wrongCodeVerifier_returnsInvalidGrant() throws Exception {
        MvcResult result = mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(clientId, clientSecret))
                        .param("grant_type", "authorization_code")
                        .param("code", "some-code")
                        .param("redirect_uri", redirectUrlClient)
                        .param("code_verifier", "wrong-verifier")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andReturn();

        Map<String, Object> error = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
        assertThat(error.get("error")).isEqualTo("invalid_grant");
    }
}
