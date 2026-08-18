package com.idp.developer.security;

import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;

public class FreeLoginPasswordEncoder implements PasswordEncoder {

    private final PasswordEncoder delegate;

    public FreeLoginPasswordEncoder(PasswordEncoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public @Nullable String encode(@Nullable CharSequence rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(@Nullable CharSequence rawPassword, @Nullable String encodedPassword) {
        return true;
    }
}
