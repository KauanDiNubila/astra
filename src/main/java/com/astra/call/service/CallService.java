package com.astra.call.service;

import com.astra.call.dto.ActiveCallResponse;
import com.astra.call.dto.CallEvent;
import com.astra.call.dto.CallParticipant;
import com.astra.call.dto.SignalRequest;
import com.astra.call.dto.StartCallRequest;
import com.astra.chat.service.ChatGroupService;
import com.astra.shared.exception.ConflictException;
import com.astra.shared.exception.NotFoundException;
import com.astra.social.service.FriendshipService;
import com.astra.user.repository.UserRepository;
import com.astra.user.service.UserService;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Service
public class CallService {

    static final int MAX_PARTICIPANTS = 5;
    static final long RING_TIMEOUT_SECONDS = 45;
    static final long DISCONNECT_GRACE_SECONDS = 30;
    static final int MAX_SIGNALS_PER_WINDOW = 400;
    static final long SIGNAL_WINDOW_MILLIS = 10_000;
    static final Set<String> SIGNAL_TYPES = Set.of("offer", "answer", "ice", "meta");

    private final FriendshipService friendshipService;
    private final ChatGroupService chatGroupService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "call-scheduler");
        thread.setDaemon(true);
        return thread;
    });

    private final Map<UUID, Call> calls = new HashMap<>();
    private final Map<UUID, UUID> callByUser = new HashMap<>();
    private final Map<UUID, UUID> callByGroup = new HashMap<>();
    private final Map<UUID, SignalWindow> signalWindows = new HashMap<>();

    public CallService(FriendshipService friendshipService, ChatGroupService chatGroupService,
            UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.friendshipService = friendshipService;
        this.chatGroupService = chatGroupService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @PreDestroy
    void shutdown() {
        scheduler.shutdownNow();
    }

    public synchronized void start(UUID me, String sessionId, StartCallRequest request) {
        boolean direct = request.targetUserId() != null;
        if (direct == (request.groupId() != null)) {
            throw new ConflictException("Informe um amigo ou um grupo");
        }
        if (callByUser.containsKey(me)) {
            throw new ConflictException("Você já está em uma call");
        }

        Set<UUID> eligible = new HashSet<>();
        UUID groupId = request.groupId();
        String groupName = null;
        if (direct) {
            UUID target = request.targetUserId();
            if (target.equals(me)) {
                throw new ConflictException("Não é possível ligar para si mesmo");
            }
            if (!friendshipService.areFriends(me, target)) {
                throw new ConflictException("Você só pode ligar para amigos");
            }
            if (callByUser.containsKey(target)) {
                throw new ConflictException("Essa pessoa está em outra call");
            }
            eligible.add(me);
            eligible.add(target);
        } else {
            List<UUID> members = chatGroupService.memberIds(groupId);
            if (!members.contains(me)) {
                throw new NotFoundException("Grupo não encontrado");
            }
            UUID existing = callByGroup.get(groupId);
            if (existing != null) {
                join(me, sessionId, existing, request.clientId());
                return;
            }
            eligible.addAll(members);
            groupName = chatGroupService.groupName(groupId);
        }

        Call call = new Call(UUID.randomUUID(), groupId, me, eligible);
        calls.put(call.id, call);
        callByUser.put(me, call.id);
        if (groupId != null) {
            callByGroup.put(groupId, call.id);
        }
        String name = nameOf(me);
        call.members.put(me, new Member(request.clientId(), sessionId, name));
        call.ringTimeout = scheduler.schedule(() -> expire(call.id), RING_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        send(me, CallEvent.started(call.id, groupId, call.participants()));
        for (UUID other : eligible) {
            if (!other.equals(me)) {
                send(other, CallEvent.incoming(call.id, groupId, groupName, me, name));
            }
        }
    }

    public synchronized void join(UUID me, String sessionId, UUID callId, String clientId) {
        Call call = calls.get(callId);
        if (call == null) {
            send(me, CallEvent.ended(callId, "ended"));
            return;
        }
        if (!call.eligible.contains(me)) {
            throw new NotFoundException("Call não encontrada");
        }
        UUID current = callByUser.get(me);
        if (current != null && !current.equals(callId)) {
            throw new ConflictException("Você já está em uma call");
        }

        Member existing = call.members.get(me);
        if (existing != null) {
            if (!existing.clientId.equals(clientId)) {
                throw new ConflictException("Você já está nessa call em outra aba");
            }
            existing.sessionId = sessionId;
            send(me, CallEvent.state(call.id, call.groupId, call.participants()));
            return;
        }
        if (call.members.size() >= MAX_PARTICIPANTS) {
            throw new ConflictException("A call está cheia (máximo " + MAX_PARTICIPANTS + " pessoas)");
        }

        Member member = new Member(clientId, sessionId, nameOf(me));
        call.members.put(me, member);
        callByUser.put(me, call.id);
        if (call.members.size() >= 2 && call.ringTimeout != null) {
            call.ringTimeout.cancel(false);
            call.ringTimeout = null;
        }

        send(me, CallEvent.state(call.id, call.groupId, call.participants()));
        CallParticipant participant = new CallParticipant(me, clientId, member.name);
        for (UUID other : call.members.keySet()) {
            if (!other.equals(me)) {
                send(other, CallEvent.joined(call.id, participant));
            }
        }
    }

    public synchronized void decline(UUID me, UUID callId) {
        Call call = calls.get(callId);
        if (call == null || !call.eligible.contains(me) || call.members.containsKey(me)) {
            return;
        }
        send(me, CallEvent.dismissed(callId));
        for (UUID member : call.members.keySet()) {
            send(member, CallEvent.declined(callId, me));
        }
        if (call.groupId == null) {
            end(call, "declined");
        }
    }

    public synchronized void leave(UUID me, UUID callId, String clientId) {
        Call call = calls.get(callId);
        if (call == null) {
            return;
        }
        Member member = call.members.get(me);
        if (member == null || !member.clientId.equals(clientId)) {
            return;
        }
        removeMember(call, me, member);
    }

    public synchronized void relay(UUID me, SignalRequest request) {
        if (!SIGNAL_TYPES.contains(request.type())) {
            throw new ConflictException("Sinal inválido");
        }
        if (!allowSignal(me)) {
            throw new ConflictException("Muitos sinais em pouco tempo");
        }
        Call call = calls.get(request.callId());
        Member sender = call == null ? null : call.members.get(me);
        if (sender == null) {
            throw new NotFoundException("Call não encontrada");
        }
        for (Map.Entry<UUID, Member> entry : call.members.entrySet()) {
            if (entry.getValue().clientId.equals(request.toClient()) && !entry.getKey().equals(me)) {
                send(entry.getKey(), CallEvent.signal(call.id, me, sender.clientId, request.type(), request.data()));
                return;
            }
        }
        throw new NotFoundException("Participante não encontrado");
    }

    public synchronized List<ActiveCallResponse> activeFor(UUID me) {
        List<ActiveCallResponse> result = new ArrayList<>();
        for (Call call : calls.values()) {
            if (call.groupId != null && call.eligible.contains(me) && !call.members.containsKey(me)) {
                result.add(new ActiveCallResponse(call.id, call.groupId, call.members.size()));
            }
        }
        return result;
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        List<Runnable> checks = new ArrayList<>();
        synchronized (this) {
            for (Call call : calls.values()) {
                for (Map.Entry<UUID, Member> entry : call.members.entrySet()) {
                    Member member = entry.getValue();
                    if (sessionId.equals(member.sessionId)) {
                        UUID callId = call.id;
                        UUID userId = entry.getKey();
                        String clientId = member.clientId;
                        checks.add(() -> dropIfStale(callId, userId, clientId, sessionId));
                    }
                }
            }
        }
        for (Runnable check : checks) {
            scheduler.schedule(check, DISCONNECT_GRACE_SECONDS, TimeUnit.SECONDS);
        }
    }

    private synchronized void dropIfStale(UUID callId, UUID userId, String clientId, String sessionId) {
        Call call = calls.get(callId);
        Member member = call == null ? null : call.members.get(userId);
        if (member != null && member.clientId.equals(clientId) && sessionId.equals(member.sessionId)) {
            removeMember(call, userId, member);
        }
    }

    private synchronized void expire(UUID callId) {
        Call call = calls.get(callId);
        if (call != null && call.members.size() < 2) {
            end(call, "no-answer");
        }
    }

    private void removeMember(Call call, UUID userId, Member member) {
        call.members.remove(userId);
        callByUser.remove(userId);
        for (UUID other : call.members.keySet()) {
            send(other, CallEvent.left(call.id, userId, member.clientId));
        }
        boolean lastOneStanding = call.groupId == null ? call.members.size() < 2 : call.members.isEmpty();
        if (lastOneStanding) {
            end(call, "left");
        }
    }

    private void end(Call call, String reason) {
        if (call.ringTimeout != null) {
            call.ringTimeout.cancel(false);
        }
        calls.remove(call.id);
        if (call.groupId != null) {
            callByGroup.remove(call.groupId, call.id);
        }
        for (UUID member : call.members.keySet()) {
            callByUser.remove(member, call.id);
        }
        for (UUID user : call.eligible) {
            send(user, CallEvent.ended(call.id, reason));
        }
    }

    private boolean allowSignal(UUID userId) {
        long now = System.currentTimeMillis();
        SignalWindow window = signalWindows.computeIfAbsent(userId, key -> new SignalWindow(now));
        if (now - window.start > SIGNAL_WINDOW_MILLIS) {
            window.start = now;
            window.count = 0;
        }
        window.count++;
        if (signalWindows.size() > 1000) {
            signalWindows.values().removeIf(w -> now - w.start > SIGNAL_WINDOW_MILLIS);
        }
        return window.count <= MAX_SIGNALS_PER_WINDOW;
    }

    private String nameOf(UUID userId) {
        UserRepository.NameBioView view = userService.nameBioByIds(List.of(userId)).get(userId);
        return view != null ? view.getName() : "";
    }

    private void send(UUID userId, CallEvent event) {
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/call", event);
    }

    private static final class Call {
        final UUID id;
        final UUID groupId;
        final UUID creatorId;
        final Set<UUID> eligible;
        final Map<UUID, Member> members = new LinkedHashMap<>();
        ScheduledFuture<?> ringTimeout;

        Call(UUID id, UUID groupId, UUID creatorId, Set<UUID> eligible) {
            this.id = id;
            this.groupId = groupId;
            this.creatorId = creatorId;
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
