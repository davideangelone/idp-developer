package com.idp.developer.service;

import java.util.List;

import com.idp.developer.mapper.OAuth2ClaimDtoMapper;
import com.idp.developer.model.OAuth2ClaimDto;
import com.idp.developer.repository.OAuth2ClaimRepository;
import org.springframework.stereotype.Service;

@Service
public class OAuth2ClaimService {

    private final OAuth2ClaimRepository oAuth2ClaimRepository;
    private final OAuth2ClaimDtoMapper oAuth2ClaimDtoMapper;

    public OAuth2ClaimService(OAuth2ClaimRepository oAuth2ClaimRepository, OAuth2ClaimDtoMapper oAuth2ClaimDtoMapper) {
        this.oAuth2ClaimRepository = oAuth2ClaimRepository;
        this.oAuth2ClaimDtoMapper = oAuth2ClaimDtoMapper;
    }

    public List<OAuth2ClaimDto> getScopedClaims() {
        return oAuth2ClaimRepository.findByScopeIsNotNull().stream()
                .map(oAuth2ClaimDtoMapper::toDto)
                .toList();
    }

    public List<OAuth2ClaimDto> getAlwaysClaims() {
        return oAuth2ClaimRepository.findByAlwaysTrue().stream()
                .map(oAuth2ClaimDtoMapper::toDto)
                .toList();
    }
}
