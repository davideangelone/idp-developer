package com.idp.developer.integration;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InvalidAuthorizationCodeTest extends AbstractIdpIntegrationMockMvcTest {

    @Test
    @DisplayName("Authorization code inesistente restituisce invalid_grant")
    void nonExistentCode_returnsInvalidGrant() throws Exception {
        MvcResult result = mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(getOauth2Client().getClientId(), getOauth2Client().getClientSecret()))
                        .param("grant_type", "authorization_code")
                        .param("code", "non-existent-code")
                        .param("redirect_uri", getOauth2Client().getRedirectUris().getFirst())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andReturn();

        Map<String, Object> error = parseJson(result.getResponse().getContentAsString());
        assertThat(error).containsEntry("error", "invalid_grant");
    }

    @Test
    @DisplayName("Code verifier errato restituisce invalid_grant")
    void wrongCodeVerifier_returnsInvalidGrant() throws Exception {
        MvcResult result = mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(getOauth2Client().getClientId(), getOauth2Client().getClientSecret()))
                        .param("grant_type", "authorization_code")
                        .param("code", "some-code")
                        .param("redirect_uri", getOauth2Client().getRedirectUris().getFirst())
                        .param("code_verifier", "wrong-verifier")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andReturn();

        Map<String, Object> error = parseJson(result.getResponse().getContentAsString());
        assertThat(error).containsEntry("error", "invalid_grant");
    }
}
