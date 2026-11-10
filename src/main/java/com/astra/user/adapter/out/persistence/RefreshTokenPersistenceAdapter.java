package com.astra.user.adapter.out.persistence;

import com.astra.user.application.port.out.CreateRefreshTokenPort;
import com.astra.user.application.port.out.LoadRefreshTokenPort;
import com.astra.user.application.port.out.RevokeAllRefreshTokensPort;
import com.astra.user.application.port.out.UpdateRefreshTokenPort;
import com.astra.user.domain.RefreshToken;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class RefreshTokenPersistenceAdapter implements CreateRefreshTokenPort, LoadRefreshTokenPort, UpdateRefreshTokenPort,
        RevokeAllRefreshTokensPort {

    private final SpringDataRefreshTokenRepository repository;

    RefreshTokenPersistenceAdapter(SpringDataRefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public void create(RefreshToken token) {
        repository.save(new RefreshTokenJpaEntity(token.getUserId(), token.getTokenHash(), token.getExpiresAt()));
    }

    @Override
    public Optional<RefreshToken> loadByHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash)
                .map(e -> RefreshToken.withId(e.getId(), e.getUserId(), e.getTokenHash(), e.getExpiresAt(),
                        e.getRevokedAt(), e.isRotated()));
    }

    @Override
    public void update(RefreshToken token) {
        RefreshTokenJpaEntity entity = repository.findById(token.getId()).orElseThrow();
        entity.setRevokedAt(token.getRevokedAt());
        entity.setRotated(token.isRotated());
    }

    @Override
    public void revokeAllFor(UUID userId, OffsetDateTime now) {
        repository.revokeAllForUser(userId, now);
    }
}
