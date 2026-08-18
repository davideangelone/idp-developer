package com.idp.developer.model;

public record OAuth2ClaimDto(
        Long id,
        Long scopeId,
        String name,
        String userProperty
) {
}
