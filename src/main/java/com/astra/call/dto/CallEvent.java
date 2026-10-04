package com.astra.call.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CallEvent(
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

    public static CallEvent started(UUID callId, UUID groupId, List<CallParticipant> participants) {
        return new CallEvent("started", callId, groupId, null, null, null, null, null, null, null, null, participants);
    }

    public static CallEvent incoming(UUID callId, UUID groupId, String groupName, UUID fromUserId, String fromName) {
        return new CallEvent("incoming", callId, groupId, groupName, fromUserId, fromName, null, null, null, null,
                null, null);
    }

    public static CallEvent state(UUID callId, UUID groupId, List<CallParticipant> participants) {
        return new CallEvent("state", callId, groupId, null, null, null, null, null, null, null, null, participants);
    }

    public static CallEvent joined(UUID callId, CallParticipant participant) {
        return new CallEvent("joined", callId, null, null, participant.userId(), participant.name(),
                participant.clientId(), null, null, null, null, null);
    }

    public static CallEvent left(UUID callId, UUID userId, String clientId) {
        return new CallEvent("left", callId, null, null, userId, null, clientId, null, null, null, null, null);
    }

    public static CallEvent declined(UUID callId, UUID userId) {
        return new CallEvent("declined", callId, null, null, userId, null, null, null, null, null, null, null);
    }

    public static CallEvent dismissed(UUID callId) {
        return new CallEvent("dismissed", callId, null, null, null, null, null, null, null, null, null, null);
    }

    public static CallEvent ended(UUID callId, String reason) {
        return new CallEvent("ended", callId, null, null, null, null, null, reason, null, null, null, null);
    }

    public static CallEvent signal(UUID callId, UUID fromUserId, String fromClient, String signalType, String data) {
        return new CallEvent("signal", callId, null, null, fromUserId, null, fromClient, null, null, signalType,
                data, null);
    }

    public static CallEvent error(String message) {
        return new CallEvent("error", null, null, null, null, null, null, null, message, null, null, null);
    }
}
