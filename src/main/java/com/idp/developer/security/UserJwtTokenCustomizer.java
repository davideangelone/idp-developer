package com.idp.developer.security;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.idp.developer.entity.User;
import com.idp.developer.mapper.UserDtoMapper;
import com.idp.developer.model.OAuth2ClaimDto;
import com.idp.developer.model.UserDto;
import com.idp.developer.service.CustomUserDetailsService;
import com.idp.developer.service.OAuth2ClaimService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserJwtTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private static final Map<String, Function<UserDto, Object>> USER_PROPERTIES = Map.of(
            "roles", UserDto::roles,
            "groups", UserDto::groups,
            "fullName", UserDto::getFullName,
            "firstName", UserDto::firstName,
            "lastName", UserDto::lastName,
            "username", UserDto::username,
            "email", UserDto::email,
            "emailVerified", UserDto::emailVerified,
            "address", UserDto::address,
            "phoneNumber", UserDto::phoneNumber
    );

    private final UserDtoMapper userDtoMapper;
    private final OAuth2ClaimService oAuth2ClaimService;

    public UserJwtTokenCustomizer(UserDtoMapper userDtoMapper, OAuth2ClaimService oAuth2ClaimService) {
        this.userDtoMapper = userDtoMapper;
        this.oAuth2ClaimService = oAuth2ClaimService;
    }

    @Override
    public void customize(JwtEncodingContext context) {
        var principal = context.getPrincipal();
        if (null == principal) {
            return;
        }

        if (principal.getPrincipal() instanceof CustomUserDetailsService.CustomUserDetails(User user)) {
            var userDto = userDtoMapper.toDto(user);
            addUserClaims(context, userDto);
        }

        if (principal.getName() != null) {
            logToken(principal.getName(), context);
        }
    }

    private void logToken(String username, JwtEncodingContext context) {
        log.info("Generato token: user={}, tokenType={}, clientId={}, grantType={}, scopes={}, claims={}",
                username,
                context.getTokenType().getValue(),
                context.getRegisteredClient().getClientId(),
                Optional.ofNullable(context.getAuthorizationGrantType())
                        .map(AuthorizationGrantType::getValue)
                        .orElse(null),
                context.getAuthorizedScopes(),
                context.getClaims().build().getClaims()
        );
    }

    private void addUserClaims(JwtEncodingContext context, UserDto userDto) {
        context.getClaims().subject(String.valueOf(userDto.id()));

        Map<String, List<OAuth2ClaimDto>> scopedClaimsMap = oAuth2ClaimService.getScopedClaimsMap();
        List<OAuth2ClaimDto> alwaysClaims = oAuth2ClaimService.getAlwaysClaims();
        List<OAuth2ClaimDto> allClaims = oAuth2ClaimService.getAllClaims();

        Set<String> authorizedClaims = context.getAuthorizedScopes()
                .stream()
                .flatMap(scope -> scopedClaimsMap
                        .getOrDefault(scope, List.of())
                        .stream())
                .map(OAuth2ClaimDto::name)
                .collect(Collectors.toSet());

        authorizedClaims.addAll(alwaysClaims.stream()
                .map(OAuth2ClaimDto::name)
                .collect(Collectors.toSet()));

        Map<String, String> authorizedMappings = allClaims
                .stream()
                .filter(claim -> authorizedClaims.contains(claim.name()))
                .collect(Collectors.toMap(
                        OAuth2ClaimDto::name,
                        OAuth2ClaimDto::userProperty
                ));

        addMappings(context, userDto, authorizedMappings);
    }

    private void addMappings(JwtEncodingContext context, UserDto userDto, Map<String, String> mappings) {
        mappings.forEach((claim, property) -> {
            var resolver = USER_PROPERTIES.get(property);
            if (resolver != null) {
                context.getClaims().claim(claim, resolver.apply(userDto));
            } else {
                log.warn("User property non valida per il claim JWT {}", property);
            }
        });
    }
}