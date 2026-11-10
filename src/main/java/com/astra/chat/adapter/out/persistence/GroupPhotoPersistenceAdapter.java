package com.astra.chat.adapter.out.persistence;

import com.astra.chat.application.port.out.LoadGroupPhotoPort;
import com.astra.chat.application.port.out.UpdateGroupPhotoPort;
import com.astra.chat.domain.ChatImage;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class GroupPhotoPersistenceAdapter implements LoadGroupPhotoPort, UpdateGroupPhotoPort {

    private final SpringDataGroupPhotoRepository photos;

    GroupPhotoPersistenceAdapter(SpringDataGroupPhotoRepository photos) {
        this.photos = photos;
    }

    @Override
    public Optional<ChatImage> load(UUID groupId) {
        return photos.findById(groupId)
                .filter(p -> p.getAvatar() != null)
                .map(p -> new ChatImage(p.getAvatarContentType(), p.getAvatar()));
    }

    @Override
    public void update(UUID groupId, ChatImage photo) {
        GroupPhotoJpaEntity entity = photos.findById(groupId).orElseThrow();
        entity.setAvatar(photo.bytes());
        entity.setAvatarContentType(photo.contentType());
    }
}
