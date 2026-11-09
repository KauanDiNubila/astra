package com.astra.user.adapter.out.persistence;

import com.astra.user.application.port.out.AvatarPort;
import com.astra.user.domain.Avatar;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class AvatarPersistenceAdapter implements AvatarPort {

    private final SpringDataUserAvatarRepository repository;

    AvatarPersistenceAdapter(SpringDataUserAvatarRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Avatar> load(UUID userId) {
        return repository.findById(userId)
                .filter(entity -> entity.getAvatar() != null)
                .map(entity -> new Avatar(entity.getAvatar(), entity.getAvatarContentType()));
    }

    @Override
    public void save(UUID userId, Avatar avatar) {
        UserAvatarJpaEntity entity = repository.findById(userId).orElseThrow();
        entity.setAvatar(avatar.bytes());
        entity.setAvatarContentType(avatar.contentType());
    }
}
