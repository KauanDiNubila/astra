package com.astra.call.adapter.in.websocket;

import com.astra.call.domain.CallEvent;
import com.astra.call.domain.CallParticipant;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CallEventMessage(
        String type,
        UUID callId,
        UUID groupId,
        String groupName,
        UUID userId,
        String userName,
        String clientId,
        String reason,
        String message,
        String signalType,
        String data,
        List<CallParticipant> participants
) {

    static CallEventMessage from(CallEvent event) {
        return new CallEventMessage(event.type(), event.callId(), event.groupId(), event.groupName(), event.userId(),
                event.userName(), event.clientId(), event.reason(), event.message(), event.signalType(), event.data(),
                event.participants());
    }
}
