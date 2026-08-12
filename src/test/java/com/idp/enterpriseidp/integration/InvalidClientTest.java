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

class InvalidClientTest extends AbstractIdpIntegrationMockMvcTest {

    private Map<String, Object> errorBody(MvcResult result) throws Exception {
        return new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
    }

    @Test
    @DisplayName("Client ID errato restituisce invalid_client (401)")
    void invalidClientId_returnsError() throws Exception {
        MvcResult result = mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic("wrong-client", oAuth2Properties.getClientSecret()))
                        .param("grant_type", "client_credentials")
                        .param("scope", "openid")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();

        assertThat(errorBody(result)).containsEntry("error", "invalid_client");
    }

    @Test
    @DisplayName("Client secret errato restituisce invalid_client (401)")
    void invalidClientSecret_returnsError() throws Exception {
        MvcResult result = mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(oAuth2Properties.getClientId(), "wrong-secret"))
                        .param("grant_type", "client_credentials")
                        .param("scope", "openid")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();

        assertThat(errorBody(result)).containsEntry("error", "invalid_client");
    }

    @Test
    @DisplayName("Client authentication mancante restituisce redirect al login")
    void missingClientAuth_redirectsToLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/oauth2/token")
                        .param("grant_type", "client_credentials")
                        .param("scope", "openid")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertThat(result.getResponse().getHeader("Location")).contains("/login");
    }
}
