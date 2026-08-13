package com.idp.enterpriseidp.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LogoutTest extends AbstractIdpIntegrationMockMvcTest {

    @Test
    @DisplayName("Logout con id_token_hint non valido restituisce HTTP 400")
    void logout_withInvalidHint_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/connect/logout")
                        .param("id_token_hint", "invalid-token")
                        .param("client_id", appProperties.getOauth2().getClientId())
                        .param("post_logout_redirect_uri", appProperties.getOauth2().getPostLogoutRedirectUris().getFirst())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}
