package com.idp.developer.repository;

import java.util.List;
import java.util.Optional;

import com.idp.developer.entity.OAuth2Client;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2ClientRepository extends JpaRepository<OAuth2Client, Long> {
    Optional<OAuth2Client> findByClientId(String clientId);

    @EntityGraph(attributePaths = {"clientAuthenticationMethods", "scopes", "authorizationGrantTypes"})
    @NonNull
    @Override
    List<OAuth2Client> findAll();
}
