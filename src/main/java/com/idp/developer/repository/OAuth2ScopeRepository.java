package com.idp.developer.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.idp.developer.entity.OAuth2Scope;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2ScopeRepository extends JpaRepository<OAuth2Scope, Long> {
    @EntityGraph(attributePaths = "claims")
    Optional<OAuth2Scope> findByName(String name);

    @EntityGraph(attributePaths = "claims")
    Set<OAuth2Scope> findByNameIn(Set<String> names);

    @EntityGraph(attributePaths = "claims")
    @Override
    @NonNull List<OAuth2Scope> findAll();
}
