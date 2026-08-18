package com.idp.developer.repository;

import com.idp.developer.entity.OAuth2Claim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2ClaimRepository extends JpaRepository<OAuth2Claim, Long> {
}
