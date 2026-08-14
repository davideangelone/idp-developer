package com.idp.enterpriseidp.security;

import org.springframework.security.crypto.password.PasswordEncoder;

public class AlwaysMatchesPasswordEncoder {

    private AlwaysMatchesPasswordEncoder() {}

    public static PasswordEncoder getInstance() {

        return new PasswordEncoder() {

            @Override
            public String encode(CharSequence rawPassword) { return rawPassword.toString(); }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) { return true; }
        };
    }
}
