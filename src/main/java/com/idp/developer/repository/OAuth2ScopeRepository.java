package com.idp.developer.repository;

import java.util.Optional;

import com.idp.developer.entity.OAuth2Scope;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2ScopeRepository extends JpaRepository<OAuth2Scope, Long> {
    Optional<OAuth2Scope> findByName(String name);
}
