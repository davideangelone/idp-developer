package com.idp.developer.service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import com.idp.developer.constants.CacheNames;
import com.idp.developer.entity.OAuth2Client;
import com.idp.developer.mapper.OAuth2ClientDtoMapper;
import com.idp.developer.model.OAuth2ClientDto;
import com.idp.developer.model.OAuth2ClientUpdateDto;
import com.idp.developer.repository.OAuth2AuthenticationMethodRepository;
import com.idp.developer.repository.OAuth2ClientRepository;
import com.idp.developer.repository.OAuth2GrantTypeRepository;
import com.idp.developer.repository.OAuth2ScopeRepository;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class OAuth2ClientService {

    private final OAuth2ClientRepository oAuth2ClientRepository;
    private final OAuth2ClientDtoMapper oauth2ClientDtoMapper;
    private final OAuth2ScopeRepository oAuth2ScopeRepository;
    private final OAuth2GrantTypeRepository oAuth2GrantTypeRepository;
    private final OAuth2AuthenticationMethodRepository oAuth2AuthenticationMethodRepository;

    public OAuth2ClientService(
            OAuth2ClientRepository oAuth2ClientRepository,
            OAuth2ClientDtoMapper oauth2ClientDtoMapper,
            OAuth2ScopeRepository oAuth2ScopeRepository,
            OAuth2GrantTypeRepository oAuth2GrantTypeRepository,
            OAuth2AuthenticationMethodRepository oAuth2AuthenticationMethodRepository) {

        this.oAuth2ClientRepository = oAuth2ClientRepository;
        this.oauth2ClientDtoMapper = oauth2ClientDtoMapper;
        this.oAuth2ScopeRepository = oAuth2ScopeRepository;
        this.oAuth2GrantTypeRepository = oAuth2GrantTypeRepository;
        this.oAuth2AuthenticationMethodRepository = oAuth2AuthenticationMethodRepository;
    }

    public List<OAuth2ClientDto> getAllOAuth2Clients() {
        return oAuth2ClientRepository.findAll().stream()
                .map(oauth2ClientDtoMapper::toDto)
                .toList();
    }

    @CacheEvict(value = CacheNames.REGISTERED_CLIENTS, allEntries = true)
    @Transactional
    public void updateOAuth2Client(OAuth2ClientUpdateDto dto) {

        OAuth2Client client = oAuth2ClientRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("OAuth2 client non trovato: " + dto.id()));

        client.setClientId(dto.clientId());
        client.setClientSecret(dto.clientSecret());
        client.setDescription(dto.description());
        client.setClientUrl(dto.clientUrl());
        client.setRedirectUris(dto.redirectUris());
        client.setPostLogoutRedirectUris(dto.postLogoutRedirectUris());
        client.setScopes(oAuth2ScopeRepository.findByNameIn(dto.scopes()));
        client.setAuthorizationGrantTypes(oAuth2GrantTypeRepository.findByNameIn(dto.authorizationGrantTypes()));
        client.setClientAuthenticationMethods(oAuth2AuthenticationMethodRepository.findByNameIn(dto.clientAuthenticationMethods()));
        client.setAuthorizationConsent(dto.authorizationConsent());
        client.setRequireProofKey(dto.requireProofKey());
        client.setAccessTokenTtl(dto.accessTokenTtl());
        client.setRefreshTokenTtl(dto.refreshTokenTtl());
        client.setAuthorizationCodeTtl(dto.authorizationCodeTtl());
        client.setReuseRefreshTokens(dto.reuseRefreshTokens());

        oAuth2ClientRepository.save(client);
    }

    @CacheEvict(value = CacheNames.REGISTERED_CLIENTS, allEntries = true)
    @Transactional
    public void deleteOAuth2Client(Set<Long> ids) {
        oAuth2ClientRepository.deleteAllById(ids);
    }

    public boolean existsByClientId(String clientId) {
        return oAuth2ClientRepository.existsByClientId(clientId);
    }

    @CacheEvict(value = CacheNames.REGISTERED_CLIENTS, key = "#clientId")
    @Transactional
    public OAuth2ClientDto createOAuth2Client(String clientId) {

        if (oAuth2ClientRepository.existsByClientId(clientId)) {
            throw new IllegalArgumentException("OAuth2 già esistente: " + clientId);
        }

        OAuth2Client client = new OAuth2Client();
        client.setClientId(clientId);
        client.setClientSecret("");
        client.setDescription("");
        client.setClientUrl("");
        client.setRedirectUris(List.of());
        client.setPostLogoutRedirectUris(List.of());
        client.setScopes(Set.of());
        client.setAuthorizationGrantTypes(Set.of());
        client.setClientAuthenticationMethods(Set.of());
        client.setAuthorizationConsent(false);
        client.setRequireProofKey(false);
        client.setReuseRefreshTokens(false);
        client.setAccessTokenTtl(Duration.ZERO);
        client.setRefreshTokenTtl(Duration.ZERO);
        client.setAuthorizationCodeTtl(Duration.ZERO);

        OAuth2Client saved = oAuth2ClientRepository.save(client);
        return oauth2ClientDtoMapper.toDto(saved);
    }
}
