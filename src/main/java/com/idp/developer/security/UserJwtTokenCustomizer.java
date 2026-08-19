package com.idp.developer.security;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.idp.developer.entity.User;
import com.idp.developer.mapper.UserDtoMapper;
import com.idp.developer.model.UserDto;
import com.idp.developer.properties.ConfigProperties;
import com.idp.developer.service.CustomUserDetailsService;
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

    private final ConfigProperties configProperties;
    private final UserDtoMapper userDtoMapper;

    public UserJwtTokenCustomizer(ConfigProperties configProperties, UserDtoMapper userDtoMapper) {
        this.configProperties = configProperties;
        this.userDtoMapper = userDtoMapper;
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
        addMappings(context, userDto, configProperties.getClaims().getAlways());

        for (String scope : context.getAuthorizedScopes()) {
            var mappings = configProperties.getClaims().getScopes().get(scope);
            if (mappings != null) {
                addMappings(context, userDto, mappings);
            }
        }
    }

    private void addMappings(JwtEncodingContext context, UserDto userDto, Map<String, String> mappings) {
        mappings.forEach((claim, property) -> {
            var resolver = USER_PROPERTIES.get(property);
            if (resolver != null) {
                context.getClaims().claim(claim, resolver.apply(userDto));
            } else {
                log.warn("Unknown User property configured for JWT claim: {}", property);
            }
        });
    }
}