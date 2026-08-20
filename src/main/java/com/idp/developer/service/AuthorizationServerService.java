package com.idp.developer.service;

import com.idp.developer.entity.AuthorizationServer;
import com.idp.developer.mapper.AuthorizationServerDtoMapper;
import com.idp.developer.model.AuthorizationServerDto;
import com.idp.developer.repository.AuthorizationServerRepository;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationServerService {

    private static final String ERRORE_AUTHORIZATION_SERVER_NON_CONFIGURATO = "Authorization server non configurato";

    private final AuthorizationServerRepository authorizationServerRepository;
    private final AuthorizationServerDtoMapper authorizationServerDtoMapper;

    @Getter
    private volatile boolean initialized;

    public AuthorizationServerService(AuthorizationServerRepository authorizationServerRepository, AuthorizationServerDtoMapper authorizationServerDtoMapper) {
        this.authorizationServerRepository = authorizationServerRepository;
        this.authorizationServerDtoMapper = authorizationServerDtoMapper;
    }

    public void markInitialized() {
        initialized = true;
    }

    public AuthorizationServerDto getAuthorizationServer() {
        return authorizationServerRepository.findFirstByOrderByIdAsc()
                .map(authorizationServerDtoMapper::toDto)
                .orElseThrow(() -> new IllegalStateException(ERRORE_AUTHORIZATION_SERVER_NON_CONFIGURATO));
    }

    public boolean isFreeLogin() {
        return authorizationServerRepository.findFirstByOrderByIdAsc()
                .map(AuthorizationServer::isFreeLogin)
                .orElseThrow(() -> new IllegalStateException(ERRORE_AUTHORIZATION_SERVER_NON_CONFIGURATO));
    }

    public void updateFreeLogin(boolean freeLogin) {
        AuthorizationServer authorizationServer = authorizationServerRepository
                .findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException(ERRORE_AUTHORIZATION_SERVER_NON_CONFIGURATO));

        authorizationServer.setFreeLogin(freeLogin);
        authorizationServerRepository.save(authorizationServer);
    }
}
