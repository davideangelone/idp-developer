package com.idp.enterpriseidp.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.idp.enterpriseidp.entity.User;
import com.idp.enterpriseidp.mapper.UserDtoMapper;
import com.idp.enterpriseidp.model.UserDto;
import com.idp.enterpriseidp.properties.ClaimsProperties;
import com.idp.enterpriseidp.properties.ConfigProperties;
import com.idp.enterpriseidp.security.UserJwtTokenCustomizer;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2TokenCustomizerTest {

    @Mock
    private JwtEncodingContext context;

    @Mock
    private Authentication authentication;

    @Mock
    private JwtClaimsSet.Builder claimsBuilder;

    @Mock
    private ConfigProperties configProperties;

    @Mock
    private UserDtoMapper userDtoMapper;

    private UserJwtTokenCustomizer userJwtTokenCustomizer;

    private UserDto userDto;

    private ClaimsProperties claimsProperties;

    @BeforeEach
    void setUp() {
        userJwtTokenCustomizer = new UserJwtTokenCustomizer(configProperties, userDtoMapper);
        userDto = new UserDto(1L, "Mario", "Rossi",
                "test", "password",
                "test@email.com", true,
                "Via Roma 1", "+39 333 1234567",
                Set.of("USER"), Set.of("users")
        );


        claimsProperties = new ClaimsProperties();

        claimsProperties.setAlways(Map.of(
                "roles", "roles",
                "groups", "groups"
        ));

        var profile = Map.of(
                "name", "fullName",
                "given_name", "firstName",
                "family_name", "lastName",
                "preferred_username", "username"
        );

        var email = Map.of(
                "email", "email",
                "email_verified", "emailVerified"
        );

        var address = Map.of(
                "address", "address"
        );

        var phone = Map.of(
                "phone_number", "phoneNumber"
        );

        claimsProperties.setScopes(Map.of(
                "profile", profile,
                "email", email,
                "address", address,
                "phone", phone
        ));
    }

    @Test
    @DisplayName("Aggiunge i claim custom quando il principal e CustomUserDetails")
    void addsCustomClaims_whenPrincipalIsCustomUserDetails() {
        CustomUserDetailsService.CustomUserDetails customUserDetails = getCustomUserDetails(userDto);

        when(context.getPrincipal()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(context.getClaims()).thenReturn(claimsBuilder);
        when(context.getAuthorizedScopes()).thenReturn(Set.of("profile", "email", "address", "phone"));
        when(userDtoMapper.toDto(any(User.class))).thenReturn(userDto);
        when(configProperties.getClaims()).thenReturn(claimsProperties);

        userJwtTokenCustomizer.customize(context);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
        verify(claimsBuilder, times(10)).claim(keyCaptor.capture(), valueCaptor.capture());
        verify(claimsBuilder, times(1)).subject(any(String.class));

        List<String> keys = keyCaptor.getAllValues();
        assertThat(keys).containsExactlyInAnyOrder(
                "name", "given_name", "family_name", "email",
                "email_verified", "address", "phone_number", "preferred_username", "roles", "groups"
        );

        assertThat(valueCaptor.getAllValues()).containsExactlyInAnyOrder(
                userDto.getFullName(),
                userDto.firstName(),
                userDto.lastName(),
                userDto.roles(),
                userDto.groups(),
                userDto.email(),
                userDto.emailVerified(),
                userDto.address(),
                userDto.phoneNumber(),
                userDto.username()
        );
    }

    private CustomUserDetailsService.CustomUserDetails getCustomUserDetails(UserDto userDto) {
        User user = new User();
        user.setId(10L);
        user.setUsername(userDto.username());
        user.setEmail(userDto.email());
        user.setPassword(userDto.password());
        user.setFirstName(userDto.firstName());
        user.setLastName(userDto.lastName());
        user.setAddress(userDto.address());
        user.setPhoneNumber(userDto.phoneNumber());
        user.setRoles(userDto.roles());
        user.setGroups(userDto.groups());
        user.setEnabled(true);
        user.setEmailVerified(true);

        return new CustomUserDetailsService.CustomUserDetails(user);
    }

    @Test
    @DisplayName("Non aggiunge claim quando il principal e null")
    void doesNothing_whenPrincipalIsNull() {
        when(context.getPrincipal()).thenReturn(null);

        userJwtTokenCustomizer.customize(context);

        verify(context, never()).getClaims();
    }

    @Test
    @DisplayName("Non aggiunge claim quando principal.getPrincipal() e null")
    void doesNothing_whenInnerPrincipalIsNull() {
        when(context.getPrincipal()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);

        userJwtTokenCustomizer.customize(context);

        verify(context, never()).getClaims();
    }

    @Test
    @DisplayName("Non aggiunge claim quando il principal non e CustomUserDetails")
    void doesNothing_whenPrincipalIsNotCustomUserDetails() {
        when(context.getPrincipal()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("some-other-principal");

        userJwtTokenCustomizer.customize(context);

        verify(context, never()).getClaims();
    }
}
