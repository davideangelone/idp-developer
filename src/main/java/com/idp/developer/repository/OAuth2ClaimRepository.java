package com.idp.developer.repository;

import java.util.List;
import java.util.Optional;

import com.idp.developer.entity.OAuth2Claim;
import com.idp.developer.entity.OAuth2Scope;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2ClaimRepository extends JpaRepository<OAuth2Claim, Long> {
    Optional<OAuth2Claim> findByScopeAndName(OAuth2Scope scope, String name);

    Optional<OAuth2Claim> findByScopeIsNullAndName(String name);

    List<OAuth2Claim> findByAlwaysTrue();

    List<OAuth2Claim> findByScopeIsNotNull();
}
