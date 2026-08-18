package com.idp.developer.service;

import java.util.List;

import com.idp.developer.mapper.OAuth2ClientDtoMapper;
import com.idp.developer.model.OAuth2ClientDto;
import com.idp.developer.repository.OAuth2ClientRepository;
import org.springframework.stereotype.Service;

@Service
public class OAuth2ClientService {

    private final OAuth2ClientRepository oAuth2ClientRepository;
    private final OAuth2ClientDtoMapper oauth2ClientDtoMapper;

    public OAuth2ClientService(OAuth2ClientRepository oAuth2ClientRepository, OAuth2ClientDtoMapper oauth2ClientDtoMapper) {
        this.oAuth2ClientRepository = oAuth2ClientRepository;
        this.oauth2ClientDtoMapper = oauth2ClientDtoMapper;
    }

    public List<OAuth2ClientDto> getAllOAuth2Clients() {
        return oAuth2ClientRepository.findAll().stream()
                .map(oauth2ClientDtoMapper::toDto)
                .toList();
    }
}
