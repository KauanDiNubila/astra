package com.astra.chat.application.service;

import com.astra.chat.application.port.in.MarkGroupReadUseCase;
import com.astra.chat.application.port.out.MarkGroupReadPort;
import com.astra.shared.exception.NotFoundException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class MarkGroupReadService implements MarkGroupReadUseCase {

    private final MarkGroupReadPort markGroupReadPort;
    private final Clock clock;

    MarkGroupReadService(MarkGroupReadPort markGroupReadPort, Clock clock) {
        this.markGroupReadPort = markGroupReadPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void markRead(UUID userId, UUID groupId) {
        if (!markGroupReadPort.markRead(groupId, userId, OffsetDateTime.now(clock))) {
            throw new NotFoundException("Grupo não encontrado");
        }
    }
}
