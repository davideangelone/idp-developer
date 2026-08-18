package com.idp.developer.repository;

import com.idp.developer.entity.OAuth2Scope;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2ScopeRepository extends JpaRepository<OAuth2Scope, Long> {
    @EntityGraph(attributePaths = "claims")
    OAuth2Scope findByName(String name);
}
