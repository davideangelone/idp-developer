package com.idp.developer.security;

import com.idp.developer.service.AuthorizationServerService;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class LoginRequestMatcher implements RequestMatcher {

    private final AuthorizationServerService service;
    private final boolean freeLoginMatch;

    public LoginRequestMatcher(AuthorizationServerService service, boolean freeLoginMatch) {
        this.service = service;
        this.freeLoginMatch = freeLoginMatch;
    }

    @Override
    public boolean matches(@NonNull HttpServletRequest request) {
        return service.isInitialized() && (freeLoginMatch == service.isFreeLogin());
    }
}