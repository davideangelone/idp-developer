package com.idp.developer.entity;

import java.util.List;
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

@Entity
@Table(name = "oauth2_client")
@Data
public class OAuth2Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String clientId;

    @Column(nullable = false)
    private String clientSecret;

    @Column(nullable = false)
    private String clientUrl;

    @Column
    private List<String> redirectUris;

    @Column
    private List<String> postLogoutRedirectUris;

    @ManyToMany
    @JoinTable(
            name = "oauth2_client_scope",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "scope_id")
    )
    private Set<OAuth2Scope> scopes;

    @ManyToMany
    @JoinTable(
            name = "oauth2_client_grant_type",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "grant_type_id")
    )
    private Set<OAuth2GrantType> grantTypes;
}
