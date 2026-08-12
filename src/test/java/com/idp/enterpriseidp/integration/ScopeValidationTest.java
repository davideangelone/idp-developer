package com.idp.enterpriseidp.integration;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ScopeValidationTest extends AbstractIdpIntegrationMockMvcTest {

    @Test
    @DisplayName("Scope non supportato restituisce invalid_scope")
    void unsupportedScope_returnsInvalidScope() throws Exception {
        MvcResult result = mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(appProperties.getOauth2().getClientId(), appProperties.getOauth2().getClientSecret()))
                        .param("grant_type", "client_credentials")
                        .param("scope", "openid profile email invalid_scope")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andReturn();

        Map<String, Object> error = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
        assertThat(error).containsEntry("error", "invalid_scope");
    }

    @Test
    @DisplayName("Provider configuration e RegisteredClient hanno scope coerenti")
    void discoveryAndClientScopes_areConsistent() throws Exception {
        MvcResult discoveryResult = mockMvc.perform(get("/.well-known/openid-configuration")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> discovery = new ObjectMapper().readValue(discoveryResult.getResponse().getContentAsString(), Map.class);
        List<String> supportedScopes = (List<String>) discovery.get("scopes_supported");

        assertThat(supportedScopes).containsExactlyInAnyOrder("openid", "profile", "email", "address", "phone");
    }
}
