package com.idp.developer.entity;

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
@Table(name = "authorization_server")
@Data
public class AuthorizationServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String issuerUrl;

    @ManyToMany
    @JoinTable(
            name = "authorization_server_authentication_method",
            joinColumns = @JoinColumn(name = "authorization_server_id"),
            inverseJoinColumns = @JoinColumn(name = "authentication_method_id")
    )
    private Set<OAuth2AuthenticationMethod> supportedAuthenticationMethods;

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

    @ManyToMany
    @JoinTable(
            name = "authorization_server_role",
            joinColumns = @JoinColumn(name = "authorization_server_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> supportedRoles;

    @ManyToMany
    @JoinTable(
            name = "authorization_server_group",
            joinColumns = @JoinColumn(name = "authorization_server_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<Group> supportedGroups;

    @Column(nullable = false)
    private boolean freeLogin;
}
