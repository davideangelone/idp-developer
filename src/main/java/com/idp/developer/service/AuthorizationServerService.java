package com.idp.developer.service;

import com.idp.developer.mapper.AuthorizationServerDtoMapper;
import com.idp.developer.model.AuthorizationServerDto;
import com.idp.developer.repository.AuthorizationServerRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationServerService {

    private final AuthorizationServerRepository authorizationServerRepository;
    private final AuthorizationServerDtoMapper authorizationServerDtoMapper;

    public AuthorizationServerService(AuthorizationServerRepository authorizationServerRepository, AuthorizationServerDtoMapper authorizationServerDtoMapper) {
        this.authorizationServerRepository = authorizationServerRepository;
        this.authorizationServerDtoMapper = authorizationServerDtoMapper;
    }

    public AuthorizationServerDto getAuthorizationServer() {
        return authorizationServerRepository.findFirstByOrderByIdAsc()
                .map(authorizationServerDtoMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Authorization server non configurato"));
    }
}
