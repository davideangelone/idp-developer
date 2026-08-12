package com.idp.enterpriseidp.security;

import java.util.Optional;

import com.idp.enterpriseidp.domain.User;
import com.idp.enterpriseidp.service.CustomUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserJwtTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {
        var principal = context.getPrincipal();

        if (principal == null || principal.getPrincipal() == null) {
            return;
        }

        Object userDetails = principal.getPrincipal();

        if (userDetails instanceof CustomUserDetailsService.CustomUserDetails(User user)) {
            addUserClaims(context, user);
        }

        if (principal.getName() != null) {
            log.info(
                    "Generating token: user={}, tokenType={}, clientId={}, grantType={}, scopes={}, claims={}",
                    principal.getName(),
                    context.getTokenType().getValue(),
                    context.getRegisteredClient().getClientId(),
                    Optional.ofNullable(context.getAuthorizationGrantType())
                            .map(AuthorizationGrantType::getValue)
                            .orElse(null),
                    context.getAuthorizedScopes(),
                    context.getClaims().build().getClaims()
            );
        }
    }

    private void addUserClaims(JwtEncodingContext context, User user) {
        context.getClaims().claim("sub", user.getId());
        context.getClaims().claim("name", user.getFirstName() + " " + user.getLastName());
        context.getClaims().claim("given_name", user.getFirstName());
        context.getClaims().claim("family_name", user.getLastName());
        context.getClaims().claim("roles", user.getRoles());
        context.getClaims().claim("groups", user.getGroups());
        context.getClaims().claim("email", user.getEmail());
        context.getClaims().claim("email_verified", user.isEmailVerified());
        context.getClaims().claim("address", user.getAddress());
        context.getClaims().claim("phone_number", user.getPhoneNumber());
        context.getClaims().claim("preferred_username", user.getUsername());
    }
}