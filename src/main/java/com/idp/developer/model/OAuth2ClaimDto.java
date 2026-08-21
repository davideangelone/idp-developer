package com.idp.developer.model;

public record OAuth2ClaimDto(
        Long id,
        boolean always,
        String name,
        String userProperty
) {
}
