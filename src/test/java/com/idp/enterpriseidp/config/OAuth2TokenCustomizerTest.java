package com.idp.enterpriseidp.config;

import java.util.List;

import com.idp.enterpriseidp.domain.User;
import com.idp.enterpriseidp.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2TokenCustomizerTest {

    @Mock
    JwtEncodingContext context;

    @Mock
    Authentication authentication;

    @Mock
    JwtClaimsSet.Builder claimsBuilder;

    AuthorizationServerConfig config;

    @BeforeEach
    void setUp() {
        config = new AuthorizationServerConfig(null);
    }

    @Test
    @DisplayName("Aggiunge i claim custom quando il principal e CustomUserDetails")
    void addsCustomClaims_whenPrincipalIsCustomUserDetails() {
        User user = new User();
        user.setId(10L);
        user.setUsername("test");
        user.setEmail("test@example.com");
        user.setPassword("encoded");
        user.setFirstName("Mario");
        user.setLastName("Rossi");
        user.setAddress("Via Roma 1");
        user.setPhoneNumber("+39 333 1234567");
        user.setEnabled(true);
        user.setEmailVerified(true);

        CustomUserDetailsService.CustomUserDetails customUserDetails =
                new CustomUserDetailsService.CustomUserDetails(user);

        when(context.getPrincipal()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(context.getClaims()).thenReturn(claimsBuilder);
        when(claimsBuilder.claim(any(String.class), any())).thenReturn(claimsBuilder);

        config.tokenCustomizer().customize(context);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
        verify(claimsBuilder, atLeast(1)).claim(keyCaptor.capture(), valueCaptor.capture());

        List<String> keys = keyCaptor.getAllValues();
        assertThat(keys).containsExactlyInAnyOrder(
                "sub", "name", "given_name", "family_name", "email",
                "email_verified", "address", "phone_number", "preferred_username"
        );

        assertThat(valueCaptor.getAllValues()).containsExactly(
                10L,
                "Mario Rossi",
                "Mario",
                "Rossi",
                "test@example.com",
                true,
                "Via Roma 1",
                "+39 333 1234567",
                "test"
        );
    }

    @Test
    @DisplayName("Non aggiunge claim quando il principal e null")
    void doesNothing_whenPrincipalIsNull() {
        when(context.getPrincipal()).thenReturn(null);

        config.tokenCustomizer().customize(context);

        verify(context, never()).getClaims();
    }

    @Test
    @DisplayName("Non aggiunge claim quando principal.getPrincipal() e null")
    void doesNothing_whenInnerPrincipalIsNull() {
        when(context.getPrincipal()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);

        config.tokenCustomizer().customize(context);

        verify(context, never()).getClaims();
    }

    @Test
    @DisplayName("Non aggiunge claim quando il principal non e CustomUserDetails")
    void doesNothing_whenPrincipalIsNotCustomUserDetails() {
        when(context.getPrincipal()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("some-other-principal");

        config.tokenCustomizer().customize(context);

        verify(context, never()).getClaims();
    }
}
