package com.idp.developer.model;

import java.util.Set;

public record UserUpdateDto(
        Long id,
        String firstName,
        String lastName,
        String password,
        String email,
        boolean emailVerified,
        String address,
        String phoneNumber,
        Set<String> roles,
        Set<String> groups,
        boolean enabled,
        boolean accountNonExpired,
        boolean accountNonLocked,
        boolean credentialsNonExpired) implements UserDtoInterface {
}
