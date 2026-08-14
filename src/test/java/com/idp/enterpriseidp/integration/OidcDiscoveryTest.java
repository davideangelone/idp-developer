package com.idp.enterpriseidp.integration;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OidcDiscoveryTest extends AbstractIdpIntegrationMockMvcTest {

    @Test
    @DisplayName("GET /.well-known/openid-configuration ritorna HTTP 200 e i campi OIDC attesi")
    void oidcDiscovery_returnsValidConfiguration() throws Exception {
        MvcResult result = mockMvc.perform(get("/.well-known/openid-configuration")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> config = parseJson(result.getResponse().getContentAsString());

        assertThat(config)
                .containsEntry("issuer", configProperties.getAuthorizationServer().getIssuerUrl())
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

        assertThat(authorizationEndpoint).startsWith(configProperties.getAuthorizationServer().getIssuerUrl());
        assertThat(tokenEndpoint).startsWith(configProperties.getAuthorizationServer().getIssuerUrl());
        assertThat(jwksUri).startsWith(configProperties.getAuthorizationServer().getIssuerUrl());
        assertThat(endSessionEndpoint).startsWith(configProperties.getAuthorizationServer().getIssuerUrl());

        if (config.containsKey("userinfo_endpoint")) {
            assertThat((String) config.get("userinfo_endpoint")).startsWith(configProperties.getAuthorizationServer().getIssuerUrl());
        }

        assertThat(config.get("scopes_supported"))
                .asInstanceOf(LIST)
                .containsExactlyInAnyOrder(
                        "openid", "profile", "email", "address", "phone"
                );

        assertThat(config.get("code_challenge_methods_supported"))
                .asInstanceOf(LIST)
                .contains("S256");

        assertThat(config.get("grant_types_supported"))
                .asInstanceOf(LIST)
                .contains(
                        "authorization_code",
                        "refresh_token",
                        "client_credentials"
                );
    }
}
