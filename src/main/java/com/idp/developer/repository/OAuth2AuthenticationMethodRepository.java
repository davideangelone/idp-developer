package com.idp.developer.repository;

import java.util.Set;

import com.idp.developer.entity.OAuth2AuthenticationMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2AuthenticationMethodRepository extends JpaRepository<OAuth2AuthenticationMethod, Long> {
    OAuth2AuthenticationMethod findByName(String name);

    Set<OAuth2AuthenticationMethod> findByNameIn(Set<String> names);
}
