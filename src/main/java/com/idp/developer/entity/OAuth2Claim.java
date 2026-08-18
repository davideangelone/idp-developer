package com.idp.developer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "oauth2_claim")
@Data
public class OAuth2Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "scope_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OAuth2Scope scope;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String userProperty;
}