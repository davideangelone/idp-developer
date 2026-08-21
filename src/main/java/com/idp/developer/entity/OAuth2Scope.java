package com.idp.developer.entity;

import java.util.HashSet;
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
@Table(name = "oauth2_scope")
@Data
public class OAuth2Scope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany
    @JoinTable(
            name = "oauth2_scope_claim",
            joinColumns = @JoinColumn(name = "scope_id"),
            inverseJoinColumns = @JoinColumn(name = "claim_id")
    )
    private Set<OAuth2Claim> claims = new HashSet<>();
}