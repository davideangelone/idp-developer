package com.idp.enterpriseidp.security;

import java.util.Map;
import java.util.Optional;

import com.idp.enterpriseidp.entity.User;
import com.idp.enterpriseidp.mapper.UserDtoMapper;
import com.idp.enterpriseidp.model.UserDto;
import com.idp.enterpriseidp.properties.ConfigProperties;
import com.idp.enterpriseidp.service.CustomUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserJwtTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private final ConfigProperties configProperties;
    private final UserDtoMapper userDtoMapper;

    public UserJwtTokenCustomizer(ConfigProperties configProperties, UserDtoMapper userDtoMapper) {
        this.configProperties = configProperties;
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public void customize(JwtEncodingContext context) {
        var principal = context.getPrincipal();
        if (principal == null || principal.getPrincipal() == null) {
            return;
        }

        Object userDetails = principal.getPrincipal();

        if (userDetails instanceof CustomUserDetailsService.CustomUserDetails(User user)) {
            UserDto userDto = userDtoMapper.toDto(user);
            addUserClaims(context, userDto);
        }

        if (principal.getName() != null) {
            logToken(principal.getName(), context);
        }
    }

    private void logToken(String username, JwtEncodingContext context) {
        log.info("Generating token: user={}, tokenType={}, clientId={}, grantType={}, scopes={}, claims={}",
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
            configProperties.getClaims().getScopes().stream()
                    .filter(s -> s.getScope().equals(scope))
                    .findFirst()
                    .ifPresent(s -> addMappings(context, userDto, s.getMappings()));
        }
    }

    private void addMappings(JwtEncodingContext context, UserDto userDto, Map<String, String> mappings) {
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            Object value = resolveUserProperty(userDto, entry.getValue());
            if (value != null) {
                context.getClaims().claim(entry.getKey(), value);
            }
        }
    }

    private Object resolveUserProperty(UserDto userDto, String property) {
        return switch (property) {
            case "roles" -> userDto.roles();
            case "groups" -> userDto.groups();
            case "fullName" -> userDto.getFullName();
            case "firstName" -> userDto.firstName();
            case "lastName" -> userDto.lastName();
            case "username" -> userDto.username();
            case "email" -> userDto.email();
            case "emailVerified" -> userDto.emailVerified();
            case "address" -> userDto.address();
            case "phoneNumber" -> userDto.phoneNumber();
            default -> {
                log.warn("Unknown User property configured for JWT claim: {}", property);
                yield null;
            }
        };
    }
}