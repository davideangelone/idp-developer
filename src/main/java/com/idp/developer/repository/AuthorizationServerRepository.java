package com.idp.developer.repository;

import java.util.Optional;

import com.idp.developer.entity.AuthorizationServer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorizationServerRepository extends JpaRepository<AuthorizationServer, Long> {

    @EntityGraph(attributePaths = {"supportedAuthenticationMethods", "supportedScopes", "supportedGrantTypes"})
    Optional<AuthorizationServer> findFirstByOrderByIdAsc();
}
