package com.astra.chat.application.service;

import com.astra.chat.application.port.in.ImageUpload;
import com.astra.chat.application.port.in.UpdateGroupPhotoUseCase;
import com.astra.chat.application.port.out.CheckGroupMemberPort;
import com.astra.chat.application.port.out.UpdateGroupPhotoPort;
import com.astra.chat.domain.ChatImage;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UpdateGroupPhotoService implements UpdateGroupPhotoUseCase {

    private final CheckGroupMemberPort checkGroupMemberPort;
    private final UpdateGroupPhotoPort updateGroupPhotoPort;

    UpdateGroupPhotoService(CheckGroupMemberPort checkGroupMemberPort, UpdateGroupPhotoPort updateGroupPhotoPort) {
        this.checkGroupMemberPort = checkGroupMemberPort;
        this.updateGroupPhotoPort = updateGroupPhotoPort;
    }

    @Override
    @Transactional
    public void update(UUID userId, UUID groupId, ImageUpload photo) {
        Access.requireMember(checkGroupMemberPort, groupId, userId);
        updateGroupPhotoPort.update(groupId, ChatImage.of(photo.contentType(), photo.bytes()));
    }
}
