package com.idp.developer.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.idp.developer.entity.OAuth2Claim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2ClaimRepository extends JpaRepository<OAuth2Claim, Long> {
    Optional<OAuth2Claim> findByName(String name);
    Set<OAuth2Claim> findByNameIn(Set<String> names);
    List<OAuth2Claim> findByAlwaysTrue();
}
