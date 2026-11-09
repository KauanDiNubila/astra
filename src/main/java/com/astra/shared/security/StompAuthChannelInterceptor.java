package com.astra.shared.security;

import com.astra.user.dto.AuthInfo;
import com.astra.user.service.UserService;
import java.security.Principal;
import java.util.Set;
import java.util.UUID;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final Set<String> DESTINATIONS_FOR_ACTIVE_USERS = Set.of("/app/call.start", "/app/call.join");

    private final JwtService jwtService;
    private final UserService userService;

    public StompAuthChannelInterceptor(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String header = accessor.getFirstNativeHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                throw new StompAuthenticationException("Token ausente");
            }
            UUID userId;
            try {
                userId = jwtService.extractUserId(header.substring(7));
            } catch (Exception ex) {
                throw new StompAuthenticationException("Token invalido");
            }
            requireActive(userId);
            accessor.setUser((Principal) () -> userId.toString());
        }
        if (accessor != null && StompCommand.SEND.equals(accessor.getCommand())
                && DESTINATIONS_FOR_ACTIVE_USERS.contains(accessor.getDestination())
                && accessor.getUser() != null) {
            requireActive(UUID.fromString(accessor.getUser().getName()));
        }
        return message;
    }

    private void requireActive(UUID userId) {
        AuthInfo info = userService.authInfo(userId);
        if (info == null || info.banned()) {
            throw new StompAuthenticationException("Conta banida");
        }
    }
}
