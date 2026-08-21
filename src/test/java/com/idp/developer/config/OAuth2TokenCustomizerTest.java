package com.idp.developer.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.idp.developer.entity.User;
import com.idp.developer.mapper.UserDtoMapper;
import com.idp.developer.model.OAuth2ClaimDto;
import com.idp.developer.model.UserDto;
import com.idp.developer.security.UserJwtTokenCustomizer;
import com.idp.developer.service.CustomUserDetailsService;
import com.idp.developer.service.OAuth2ClaimService;
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
    private UserDtoMapper userDtoMapper;

    @Mock
    private OAuth2ClaimService oAuth2ClaimService;

    private UserJwtTokenCustomizer userJwtTokenCustomizer;

    private UserDto userDto;

    Map<String, List<OAuth2ClaimDto>> scopedClaimsMap;

    private List<OAuth2ClaimDto> alwaysClaims;

    private List<OAuth2ClaimDto> allClaims;

    @BeforeEach
    void setUp() {
        userJwtTokenCustomizer = new UserJwtTokenCustomizer(userDtoMapper, oAuth2ClaimService);
        userDto = new UserDto(1L, "Mario", "Rossi",
                "test", "password",
                "test@email.com", true,
                "Via Roma 1", "+39 333 1234567",
                Set.of("USER"), Set.of("users")
        );

        alwaysClaims = List.of(
                new OAuth2ClaimDto(1L, true, "roles", "roles"),
                new OAuth2ClaimDto(2L, true, "groups", "groups")
        );

        allClaims = List.of(
                new OAuth2ClaimDto(1L, true, "roles", "roles"),
                new OAuth2ClaimDto(2L, true, "groups", "groups"),
                new OAuth2ClaimDto(3L, false, "name", "fullName"),
                new OAuth2ClaimDto(4L, false, "given_name", "firstName"),
                new OAuth2ClaimDto(5L, false, "family_name", "lastName"),
                new OAuth2ClaimDto(6L, false, "preferred_username", "username"),
                new OAuth2ClaimDto(7L, false, "email", "email"),
                new OAuth2ClaimDto(8L, false, "email_verified", "emailVerified"),
                new OAuth2ClaimDto(9L, false, "address", "address"),
                new OAuth2ClaimDto(10L, false, "phone_number", "phoneNumber")
        );

        scopedClaimsMap = Map.of(
                "profile", List.of(
                        new OAuth2ClaimDto(3L, false, "name", "fullName"),
                        new OAuth2ClaimDto(4L, false, "given_name", "firstName"),
                        new OAuth2ClaimDto(5L, false, "family_name", "lastName"),
                        new OAuth2ClaimDto(6L, false, "preferred_username", "username")
                ),
                "email", List.of(
                        new OAuth2ClaimDto(7L, false, "email", "email"),
                        new OAuth2ClaimDto(8L, false, "email_verified", "emailVerified")
                ),
                "address", List.of(
                        new OAuth2ClaimDto(9L, false, "address", "address")
                ),
                "phone", List.of(
                        new OAuth2ClaimDto(10L, false, "phone_number", "phoneNumber")
                )
        );
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
        when(oAuth2ClaimService.getAllClaims()).thenReturn(allClaims);
        when(oAuth2ClaimService.getAlwaysClaims()).thenReturn(alwaysClaims);
        when(oAuth2ClaimService.getScopedClaimsMap()).thenReturn(scopedClaimsMap);

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
