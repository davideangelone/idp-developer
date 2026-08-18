package com.idp.developer.entity;

import java.time.Duration;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "authorization_server")
@Data
@NoArgsConstructor
public class AuthorizationServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String issuerUrl;

    @Column(nullable = false)
    private boolean authorizationConsent;

    @Column(nullable = false)
    private Duration accessTokenTtl;

    @Column(nullable = false)
    private Duration refreshTokenTtl;

    @Column(nullable = false)
    private Duration authorizationCodeTtl;

    @Column(nullable = false)
    private boolean reuseRefreshTokens;

    @ManyToMany
    @JoinTable(
            name = "authorization_server_scope",
            joinColumns = @JoinColumn(name = "authorization_server_id"),
            inverseJoinColumns = @JoinColumn(name = "scope_id")
    )
    private Set<OAuth2Scope> supportedScopes;

    @ManyToMany
    @JoinTable(
            name = "authorization_server_grant_type",
            joinColumns = @JoinColumn(name = "authorization_server_id"),
            inverseJoinColumns = @JoinColumn(name = "grant_type_id")
    )
    private Set<OAuth2GrantType> supportedGrantTypes;

    @Column(nullable = false)
    private boolean freeLogin;
}
