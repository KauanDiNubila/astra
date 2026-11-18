package com.astra.user.adapter.out.persistence;

import com.astra.user.application.port.out.PasswordResetTokenPort;
import com.astra.user.domain.PasswordResetToken;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PasswordResetTokenPersistenceAdapter implements PasswordResetTokenPort {

    private final SpringDataPasswordResetTokenRepository repository;

    PasswordResetTokenPersistenceAdapter(SpringDataPasswordResetTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public void create(PasswordResetToken token) {
        repository.save(new PasswordResetTokenJpaEntity(token.getUserId(), token.getTokenHash(), token.getCreatedAt(),
                token.getExpiresAt()));
    }

    @Override
    public Optional<PasswordResetToken> loadByHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(PasswordResetTokenPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<PasswordResetToken> loadLatestFor(UUID userId) {
        return repository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .map(PasswordResetTokenPersistenceAdapter::toDomain);
    }

    @Override
    public int invalidateAllFor(UUID userId, OffsetDateTime now) {
        return repository.invalidateAllForUser(userId, now);
    }

    private static PasswordResetToken toDomain(PasswordResetTokenJpaEntity e) {
        return PasswordResetToken.withId(e.getId(), e.getUserId(), e.getTokenHash(), e.getCreatedAt(),
                e.getExpiresAt(), e.getUsedAt());
    }
}
