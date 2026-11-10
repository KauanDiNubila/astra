package com.astra.call.domain;

import com.astra.shared.exception.NotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class CallBoard {

    public static final int MAX_PARTICIPANTS = 5;
    public static final int MAX_SIGNALS_PER_WINDOW = 400;
    public static final long SIGNAL_WINDOW_MILLIS = 10_000;
    private static final int SIGNAL_WINDOW_CLEANUP_THRESHOLD = 1000;
    private static final Set<String> SIGNAL_TYPES = Set.of("offer", "answer", "ice", "meta");

    public record StartResult(List<CallNotice> notices, Optional<UUID> ringingCallId) {
    }

    private final Map<UUID, Call> calls = new HashMap<>();
    private final Map<UUID, UUID> callByUser = new HashMap<>();
    private final Map<UUID, UUID> callByGroup = new HashMap<>();
    private final Map<UUID, SignalWindow> signalWindows = new HashMap<>();

    public StartResult start(UUID me, String sessionId, String clientId, UUID targetUserId, UUID groupId,
                             CallDirectory directory) {
        boolean direct = targetUserId != null;
        if (direct == (groupId != null)) {
            throw new MissingCallTargetException();
        }
        if (callByUser.containsKey(me)) {
            throw new AlreadyInCallException();
        }
        Set<UUID> eligible = new HashSet<>();
        String groupName = null;
        if (direct) {
            if (targetUserId.equals(me)) {
                throw new CannotCallYourselfException();
            }
            if (!directory.areFriends(me, targetUserId)) {
                throw new OnlyFriendsCallException();
            }
            if (callByUser.containsKey(targetUserId)) {
                throw new CalleeBusyException();
            }
            eligible.add(me);
            eligible.add(targetUserId);
        } else {
            List<UUID> members = directory.groupMembers(groupId);
            if (!members.contains(me)) {
                throw new NotFoundException("Grupo não encontrado");
            }
            UUID existing = callByGroup.get(groupId);
            if (existing != null) {
                return new StartResult(join(me, sessionId, existing, clientId, directory), Optional.empty());
            }
            eligible.addAll(members);
            groupName = directory.groupName(groupId);
        }
        Call call = new Call(UUID.randomUUID(), groupId, eligible);
        calls.put(call.id, call);
        callByUser.put(me, call.id);
        if (groupId != null) {
            callByGroup.put(groupId, call.id);
        }
        String name = directory.nameOf(me);
        call.members.put(me, new Member(clientId, sessionId, name));
        List<CallNotice> notices = new ArrayList<>();
        notices.add(new CallNotice(me, CallEvent.started(call.id, groupId, call.participants())));
        for (UUID other : eligible) {
            if (!other.equals(me)) {
                notices.add(new CallNotice(other, CallEvent.incoming(call.id, groupId, groupName, me, name)));
            }
        }
        return new StartResult(notices, Optional.of(call.id));
    }

    public List<CallNotice> join(UUID me, String sessionId, UUID callId, String clientId, CallDirectory directory) {
        Call call = calls.get(callId);
        if (call == null) {
            return List.of(new CallNotice(me, CallEvent.ended(callId, "ended")));
        }
        if (!call.eligible.contains(me)) {
            throw new NotFoundException("Call não encontrada");
        }
        UUID current = callByUser.get(me);
        if (current != null && !current.equals(callId)) {
            throw new AlreadyInCallException();
        }
        Member existing = call.members.get(me);
        if (existing != null) {
            if (!existing.clientId.equals(clientId)) {
                throw new AlreadyInCallOnAnotherTabException();
            }
            existing.sessionId = sessionId;
            return List.of(new CallNotice(me, CallEvent.state(call.id, call.groupId, call.participants())));
        }
        if (call.members.size() >= MAX_PARTICIPANTS) {
            throw new CallFullException(MAX_PARTICIPANTS);
        }
        Member member = new Member(clientId, sessionId, directory.nameOf(me));
        call.members.put(me, member);
        callByUser.put(me, call.id);
        if (call.members.size() >= 2) {
            call.answered = true;
        }
        List<CallNotice> notices = new ArrayList<>();
        notices.add(new CallNotice(me, CallEvent.state(call.id, call.groupId, call.participants())));
        CallParticipant participant = new CallParticipant(me, clientId, member.name);
        for (UUID other : call.members.keySet()) {
            if (!other.equals(me)) {
                notices.add(new CallNotice(other, CallEvent.joined(call.id, participant)));
            }
        }
        return notices;
    }

    public List<CallNotice> decline(UUID me, UUID callId) {
        Call call = calls.get(callId);
        if (call == null || !call.eligible.contains(me) || call.members.containsKey(me)) {
            return List.of();
        }
        List<CallNotice> notices = new ArrayList<>();
        notices.add(new CallNotice(me, CallEvent.dismissed(callId)));
        for (UUID member : call.members.keySet()) {
            notices.add(new CallNotice(member, CallEvent.declined(callId, me)));
        }
        if (call.groupId == null) {
            notices.addAll(end(call, "declined"));
        }
        return notices;
    }

    public List<CallNotice> leave(UUID me, UUID callId, String clientId) {
        Call call = calls.get(callId);
        Member member = call == null ? null : call.members.get(me);
        if (member == null || !member.clientId.equals(clientId)) {
            return List.of();
        }
        return removeMember(call, me, member);
    }

    public List<CallNotice> relay(UUID me, UUID callId, String toClient, String type, String data, long nowMillis) {
        if (!SIGNAL_TYPES.contains(type)) {
            throw new InvalidSignalException();
        }
        if (!allowSignal(me, nowMillis)) {
            throw new TooManySignalsException();
        }
        Call call = calls.get(callId);
        Member sender = call == null ? null : call.members.get(me);
        if (sender == null) {
            throw new NotFoundException("Call não encontrada");
        }
        for (Map.Entry<UUID, Member> entry : call.members.entrySet()) {
            if (entry.getValue().clientId.equals(toClient) && !entry.getKey().equals(me)) {
                return List.of(new CallNotice(entry.getKey(),
                        CallEvent.signal(call.id, me, sender.clientId, type, data)));
            }
        }
        throw new NotFoundException("Participante não encontrado");
    }

    public List<ActiveCall> activeFor(UUID me) {
        List<ActiveCall> result = new ArrayList<>();
        for (Call call : calls.values()) {
            if (call.groupId != null && call.eligible.contains(me) && !call.members.containsKey(me)) {
                result.add(new ActiveCall(call.id, call.groupId, call.members.size()));
            }
        }
        return result;
    }

    public List<StaleMember> membersOnSession(String sessionId) {
        List<StaleMember> result = new ArrayList<>();
        for (Call call : calls.values()) {
            for (Map.Entry<UUID, Member> entry : call.members.entrySet()) {
                if (sessionId.equals(entry.getValue().sessionId)) {
                    result.add(new StaleMember(call.id, entry.getKey(), entry.getValue().clientId, sessionId));
                }
            }
        }
        return result;
    }

    public List<CallNotice> dropIfStale(StaleMember stale) {
        Call call = calls.get(stale.callId());
        Member member = call == null ? null : call.members.get(stale.userId());
        if (member != null && member.clientId.equals(stale.clientId()) && stale.sessionId().equals(member.sessionId)) {
            return removeMember(call, stale.userId(), member);
        }
        return List.of();
    }

    public List<CallNotice> expire(UUID callId) {
        Call call = calls.get(callId);
        if (call != null && !call.answered && call.members.size() < 2) {
            return end(call, "no-answer");
        }
        return List.of();
    }

    private List<CallNotice> removeMember(Call call, UUID userId, Member member) {
        call.members.remove(userId);
        callByUser.remove(userId);
        List<CallNotice> notices = new ArrayList<>();
        for (UUID other : call.members.keySet()) {
            notices.add(new CallNotice(other, CallEvent.left(call.id, userId, member.clientId)));
        }
        boolean lastOneStanding = call.groupId == null ? call.members.size() < 2 : call.members.isEmpty();
        if (lastOneStanding) {
            notices.addAll(end(call, "left"));
        }
        return notices;
    }

    private List<CallNotice> end(Call call, String reason) {
        calls.remove(call.id);
        if (call.groupId != null) {
            callByGroup.remove(call.groupId, call.id);
        }
        for (UUID member : call.members.keySet()) {
            callByUser.remove(member, call.id);
        }
        List<CallNotice> notices = new ArrayList<>();
        for (UUID user : call.eligible) {
            notices.add(new CallNotice(user, CallEvent.ended(call.id, reason)));
        }
        return notices;
    }

    private boolean allowSignal(UUID userId, long now) {
        SignalWindow window = signalWindows.computeIfAbsent(userId, key -> new SignalWindow(now));
        if (now - window.start > SIGNAL_WINDOW_MILLIS) {
            window.start = now;
            window.count = 0;
        }
        window.count++;
        if (signalWindows.size() > SIGNAL_WINDOW_CLEANUP_THRESHOLD) {
            signalWindows.values().removeIf(w -> now - w.start > SIGNAL_WINDOW_MILLIS);
        }
        return window.count <= MAX_SIGNALS_PER_WINDOW;
    }

    private static final class Call {
        final UUID id;
        final UUID groupId;
        final Set<UUID> eligible;
        final Map<UUID, Member> members = new LinkedHashMap<>();
        boolean answered;

        Call(UUID id, UUID groupId, Set<UUID> eligible) {
            this.id = id;
            this.groupId = groupId;
            this.eligible = eligible;
        }

        List<CallParticipant> participants() {
            List<CallParticipant> list = new ArrayList<>();
            for (Map.Entry<UUID, Member> entry : members.entrySet()) {
                list.add(new CallParticipant(entry.getKey(), entry.getValue().clientId, entry.getValue().name));
            }
            return list;
        }
    }

    private static final class Member {
        final String clientId;
        final String name;
        String sessionId;

        Member(String clientId, String sessionId, String name) {
            this.clientId = clientId;
            this.sessionId = sessionId;
            this.name = name;
        }
    }

    private static final class SignalWindow {
        long start;
        int count;

        SignalWindow(long start) {
            this.start = start;
        }
    }
}
