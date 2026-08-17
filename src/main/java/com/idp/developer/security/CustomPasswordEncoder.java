package com.idp.developer.security;

import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomPasswordEncoder implements PasswordEncoder {

    private final boolean freeLogin;
    private final PasswordEncoder passwordEncoder;

    private CustomPasswordEncoder(boolean freeLogin) {
        this.freeLogin = freeLogin;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    public static PasswordEncoder getInstance(boolean freeLogin) {
        return new CustomPasswordEncoder(freeLogin);
    }

    @Override
    public @Nullable String encode(@Nullable CharSequence rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(@Nullable CharSequence rawPassword, @Nullable String encodedPassword) {
        return freeLogin || passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
