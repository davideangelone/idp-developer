package com.idp.enterpriseidp.security;

import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomPasswordEncoder implements PasswordEncoder {

    private final boolean customLogin;
    private final PasswordEncoder passwordEncoder;

    private CustomPasswordEncoder(boolean customLogin) {
        this.customLogin = customLogin;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    public static PasswordEncoder getInstance(boolean customLogin) {
        return new CustomPasswordEncoder(customLogin);
    }

    @Override
    public @Nullable String encode(@Nullable CharSequence rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(@Nullable CharSequence rawPassword, @Nullable String encodedPassword) {
        return customLogin || passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
