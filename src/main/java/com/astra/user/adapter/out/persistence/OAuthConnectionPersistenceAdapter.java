package com.astra.user.adapter.out.persistence;

import com.astra.user.application.port.out.OAuthConnectionPort;
import com.astra.user.domain.OAuthConnection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class OAuthConnectionPersistenceAdapter implements OAuthConnectionPort {

    private final SpringDataOAuthConnectionRepository repository;

    OAuthConnectionPersistenceAdapter(SpringDataOAuthConnectionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UUID> findUserId(String provider, String providerUserId) {
        return repository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(OAuthConnectionJpaEntity::getUserId);
    }

    @Override
    public void create(OAuthConnection connection) {
        repository.save(new OAuthConnectionJpaEntity(connection.userId(), connection.provider(),
                connection.providerUserId()));
    }

    @Override
    public List<OAuthConnection> listByUser(UUID userId) {
        return repository.findByUserId(userId).stream()
                .map(c -> new OAuthConnection(c.getUserId(), c.getProvider(), c.getProviderUserId(), c.getCreatedAt()))
                .toList();
    }
}
