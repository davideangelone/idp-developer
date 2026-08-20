package com.idp.developer.security;

import com.idp.developer.service.AuthorizationServerService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class LoginPasswordEncoder implements PasswordEncoder {

    private final PasswordEncoder delegate;
    private final AuthorizationServerService service;

    public LoginPasswordEncoder(PasswordEncoder delegate, AuthorizationServerService service) {
        this.delegate = delegate;
        this.service = service;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return service.isFreeLogin() || delegate.matches(rawPassword, encodedPassword);
    }
}
