package com.idp.developer.security;

import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ObjectUtils;

public class PlainTextPasswordEncoder implements PasswordEncoder {

    @Override
    public @Nullable String encode(@Nullable CharSequence rawPassword) {
        return (null == rawPassword) ? null : rawPassword.toString();
    }

    @Override
    public boolean matches(@Nullable CharSequence rawPassword, @Nullable String encodedPassword) {
        return ObjectUtils.nullSafeEquals(rawPassword, encodedPassword);
    }
}
