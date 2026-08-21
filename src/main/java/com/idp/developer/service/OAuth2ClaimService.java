package com.idp.developer.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.idp.developer.entity.OAuth2Claim;
import com.idp.developer.entity.OAuth2Scope;
import com.idp.developer.mapper.OAuth2ClaimDtoMapper;
import com.idp.developer.model.OAuth2ClaimDto;
import com.idp.developer.repository.OAuth2ClaimRepository;
import com.idp.developer.repository.OAuth2ScopeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class OAuth2ClaimService {

    private final OAuth2ClaimRepository oAuth2ClaimRepository;
    private final OAuth2ScopeRepository oAuth2ScopeRepository;
    private final OAuth2ClaimDtoMapper oAuth2ClaimDtoMapper;

    public OAuth2ClaimService(OAuth2ClaimRepository oAuth2ClaimRepository, OAuth2ScopeRepository oAuth2ScopeRepository, OAuth2ClaimDtoMapper oAuth2ClaimDtoMapper) {
        this.oAuth2ClaimRepository = oAuth2ClaimRepository;
        this.oAuth2ScopeRepository = oAuth2ScopeRepository;
        this.oAuth2ClaimDtoMapper = oAuth2ClaimDtoMapper;
    }

    public Map<String, List<OAuth2ClaimDto>> getScopedClaimsMap() {
        return oAuth2ScopeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        OAuth2Scope::getName,
                        scope -> scope.getClaims()
                                .stream()
                                .map(oAuth2ClaimDtoMapper::toDto)
                                .toList()
                ));
    }

    public List<OAuth2ClaimDto> getAlwaysClaims() {
        return oAuth2ClaimRepository.findByAlwaysTrue().stream()
                .map(oAuth2ClaimDtoMapper::toDto)
                .toList();
    }

    public List<OAuth2ClaimDto> getAllClaims() {
        return oAuth2ClaimRepository.findAll().stream()
                .map(oAuth2ClaimDtoMapper::toDto)
                .toList();
    }

    @Transactional
    public void updateScopedClaims(Map<String, Set<String>> mappings) {
        for (Map.Entry<String, Set<String>> entry : mappings.entrySet()) {
            OAuth2Scope scope = oAuth2ScopeRepository
                    .findByName(entry.getKey())
                    .orElseGet(OAuth2Scope::new);
            Set<OAuth2Claim> claims = oAuth2ClaimRepository.findByNameIn(entry.getValue());
            scope.setClaims(claims);
        }
    }
}
