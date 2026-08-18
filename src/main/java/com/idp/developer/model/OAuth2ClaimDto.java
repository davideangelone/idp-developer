package com.idp.developer.model;

public record OAuth2ClaimDto(
        Long id,
        String scope,
        boolean always,
        String name,
        String userProperty
) {
}
