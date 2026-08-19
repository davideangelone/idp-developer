package com.idp.developer.config;

import com.idp.developer.constants.CacheNames;
import com.idp.developer.mapper.OAuth2ClientRegisteredClientMapper;
import com.idp.developer.repository.OAuth2ClientRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

@Component
public class DatabaseRegisteredClientRepository implements RegisteredClientRepository {

    private final OAuth2ClientRepository oAuth2ClientRepository;
    private final OAuth2ClientRegisteredClientMapper mapper;

    public DatabaseRegisteredClientRepository(OAuth2ClientRepository oAuth2ClientRepository, OAuth2ClientRegisteredClientMapper mapper) {
        this.oAuth2ClientRepository = oAuth2ClientRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(@NonNull RegisteredClient registeredClient) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RegisteredClient findById(@NonNull String id) {
        return oAuth2ClientRepository.findById(Long.valueOf(id))
                .map(mapper::toRegisteredClient)
                .orElse(null);
    }

    @Cacheable(
            value = CacheNames.REGISTERED_CLIENTS,
            key = "#clientId"
    )
    @Override
    public RegisteredClient findByClientId(@NonNull String clientId) {
        return oAuth2ClientRepository.findByClientId(clientId)
                .map(mapper::toRegisteredClient)
                .orElse(null);
    }
}
