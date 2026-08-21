package com.idp.developer.service;

import java.util.List;

import com.idp.developer.mapper.OAuth2ScopeDtoMapper;
import com.idp.developer.model.OAuth2ScopeDto;
import com.idp.developer.repository.OAuth2ScopeRepository;
import org.springframework.stereotype.Service;

@Service
public class OAuth2ScopeService {

    private final OAuth2ScopeRepository oAuth2ScopeRepository;
    private final OAuth2ScopeDtoMapper aAuth2ScopeDtoMapper;

    public OAuth2ScopeService(OAuth2ScopeRepository oAuth2ScopeRepository, OAuth2ScopeDtoMapper aAuth2ScopeDtoMapper) {
        this.oAuth2ScopeRepository = oAuth2ScopeRepository;
        this.aAuth2ScopeDtoMapper = aAuth2ScopeDtoMapper;
    }

    public List<OAuth2ScopeDto> getAllScopes() {
        return oAuth2ScopeRepository.findAll().stream()
                .map(aAuth2ScopeDtoMapper::toDto)
                .toList();
    }

}
