package com.idp.enterpriseidp.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthorizationEndpointTest extends AbstractIdpIntegrationMockMvcTest {

    private String authorizeUrl(String query) {
        return "/oauth2/authorize?" + query;
    }

    @Test
    @DisplayName("Authorize senza autenticazione reindirizza a /login (302)")
    void authorize_unauthenticated_redirectsToLogin() throws Exception {
        String query = "response_type=code"
                + "&client_id=" + configProperties.getOauth2Client().getClientId()
                + "&redirect_uri=" + configProperties.getOauth2Client().getRedirectUris().getFirst()
                + "&scope=openid profile email"
                + "&code_challenge=" + generateCodeChallenge(generateCodeVerifier())
                + "&code_challenge_method=S256";

        mockMvc.perform(get(authorizeUrl(query)).accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    String location = result.getResponse().getHeader("Location");
                    assertThat(location).contains("/login");
                });
    }

    @Test
    @DisplayName("Authorize con response_type non supportato restituisce 400")
    void authorize_unsupportedResponseType_returnsError() throws Exception {
        String query = "response_type=token"
                + "&client_id=" + configProperties.getOauth2Client().getClientId()
                + "&redirect_uri=" + configProperties.getOauth2Client().getRedirectUris().getFirst();

        mockMvc.perform(get(authorizeUrl(query)).accept(MediaType.TEXT_HTML))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertThat(result.getResponse().getHeader("Location")).isNull());
    }

    @Test
    @DisplayName("Authorize senza PKCE e non autenticato reindirizza al login (PKCE validato post-login)")
    void authorize_missingPkce_unauthenticated_redirectsToLogin() throws Exception {
        String query = "response_type=code"
                + "&client_id=" + configProperties.getOauth2Client().getClientId()
                + "&redirect_uri=" + configProperties.getOauth2Client().getRedirectUris().getFirst()
                + "&scope=openid profile email";

        mockMvc.perform(get(authorizeUrl(query)).accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(result ->
                        assertThat(result.getResponse().getHeader("Location")).contains("/login"));
    }

    @Test
    @DisplayName("Authorize con redirect_uri non registrato restituisce 400 e non effettua redirect")
    void authorize_unregisteredRedirectUri_returnsBadRequest() throws Exception {
        String query = "response_type=code"
                + "&client_id=" + configProperties.getOauth2Client().getClientId()
                + "&redirect_uri=http://evil.example.com/callback"
                + "&scope=openid profile email"
                + "&code_challenge=" + generateCodeChallenge(generateCodeVerifier())
                + "&code_challenge_method=S256";

        mockMvc.perform(get(authorizeUrl(query)).accept(MediaType.TEXT_HTML))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertThat(result.getResponse().getHeader("Location")).isNull());
    }
}
