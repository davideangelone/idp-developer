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

    private void addUserClaims(
            JwtEncodingContext context,
            User user) {

        context.getClaims().subject(String.valueOf(user.getId()));
        context.getClaims().claim("roles", user.getRoles());
        context.getClaims().claim("groups", user.getGroups());

        for (String scope : context.getAuthorizedScopes()) {
            switch (scope) {
                case "profile" -> addProfileClaims(context, user);
                case "email" -> addEmailClaims(context, user);
                case "address" -> addAddressClaims(context, user);
                case "phone" -> addPhoneClaims(context, user);
                default -> {
                    // Nessun claim custom per questo scope
                }
            }
        }
    }

    private void addProfileClaims(JwtEncodingContext context, User user) {
        context.getClaims()
                .claim("name", user.getFirstName() + " " + user.getLastName())
                .claim("given_name", user.getFirstName())
                .claim("family_name", user.getLastName())
                .claim("preferred_username", user.getUsername());
    }

    private void addEmailClaims(JwtEncodingContext context, User user) {
        context.getClaims()
                .claim("email", user.getEmail())
                .claim("email_verified", user.isEmailVerified());
    }

    private void addAddressClaims(JwtEncodingContext context, User user) {
        context.getClaims().claim("address", user.getAddress());
    }

    private void addPhoneClaims(JwtEncodingContext context, User user) {
        context.getClaims().claim("phone_number", user.getPhoneNumber());
    }
}