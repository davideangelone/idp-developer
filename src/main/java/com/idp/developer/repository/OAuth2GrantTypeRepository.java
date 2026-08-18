package com.idp.developer.repository;

import com.idp.developer.entity.OAuth2GrantType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2GrantTypeRepository extends JpaRepository<OAuth2GrantType, Long> {
    OAuth2GrantType findByName(String name);
}
