package com.idp.enterpriseidp.model;

import java.util.Set;

public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String username,
        String password,
        String email,
        boolean emailVerified,
        String address,
        String phoneNumber,
        Set<String> roles,
        Set<String> groups) {

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
