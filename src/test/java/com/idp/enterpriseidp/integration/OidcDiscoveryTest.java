package com.idp.enterpriseidp.integration;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OidcDiscoveryTest extends AbstractIdpIntegrationMockMvcTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET /.well-known/openid-configuration ritorna HTTP 200 e i campi OIDC attesi")
    void oidcDiscovery_returnsValidConfiguration() throws Exception {
        MvcResult result = mockMvc.perform(get("/.well-known/openid-configuration")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> config = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);

        assertThat(config)
                .containsEntry("issuer", appProperties.getAuthorizationServer().getIssuerUrl())
                .containsKeys(
                    "authorization_endpoint",
                    "token_endpoint",
                    "jwks_uri",
                    "end_session_endpoint"
                );

        String authorizationEndpoint = (String) config.get("authorization_endpoint");
        String tokenEndpoint = (String) config.get("token_endpoint");
        String jwksUri = (String) config.get("jwks_uri");
        String endSessionEndpoint = (String) config.get("end_session_endpoint");

        assertThat(authorizationEndpoint).startsWith(appProperties.getAuthorizationServer().getIssuerUrl());
        assertThat(tokenEndpoint).startsWith(appProperties.getAuthorizationServer().getIssuerUrl());
        assertThat(jwksUri).startsWith(appProperties.getAuthorizationServer().getIssuerUrl());
        assertThat(endSessionEndpoint).startsWith(appProperties.getAuthorizationServer().getIssuerUrl());

        if (config.containsKey("userinfo_endpoint")) {
            assertThat((String) config.get("userinfo_endpoint")).startsWith(appProperties.getAuthorizationServer().getIssuerUrl());
        }

        Object scopesSupported = config.get("scopes_supported");
        assertThat(scopesSupported).isInstanceOf(List.class);
        List<String> scopes = (List<String>) scopesSupported;
        assertThat(scopes).containsExactlyInAnyOrder(
                "openid", "profile", "email", "address", "phone"
        );

        Object codeChallengeMethods = config.get("code_challenge_methods_supported");
        assertThat(codeChallengeMethods).isInstanceOf(List.class);
        List<String> challengeMethods = (List<String>) codeChallengeMethods;
        assertThat(challengeMethods).contains("S256");

        Object grantTypes = config.get("grant_types_supported");
        assertThat(grantTypes).isInstanceOf(List.class);
        List<String> grantList = (List<String>) grantTypes;
        assertThat(grantList).contains(
                "authorization_code", "refresh_token", "client_credentials"
        );
    }
}
