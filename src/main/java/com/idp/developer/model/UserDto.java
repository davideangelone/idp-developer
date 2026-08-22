package com.idp.developer.model;

import java.time.Instant;
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
        Set<String> groups,
        boolean enabled,
        boolean accountNonExpired,
        boolean accountNonLocked,
        boolean credentialsNonExpired,
        Instant createdAt,
        Instant updatedAt) implements UserDtoInterface {
}
