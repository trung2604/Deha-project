package com.deha.HumanResourceManagement.config.websocket;

import com.deha.HumanResourceManagement.config.security.JwtUtil;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.ChatRoom;
import com.deha.HumanResourceManagement.entity.enums.ChatRoomType;
import com.deha.HumanResourceManagement.entity.enums.Role;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.exception.UnauthorizedException;
import com.deha.HumanResourceManagement.repository.ChatRoomRepository;
import com.deha.HumanResourceManagement.repository.UserRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final JwtUtil jwtUtil;

    public WebSocketChannelInterceptor(
            UserRepository userRepository,
            ChatRoomRepository chatRoomRepository,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null) {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                User user = resolveUserFromConnect(accessor);
                UsernamePasswordAuthenticationToken auth = buildAuthentication(user);
                accessor.setUser(auth);
                Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                if (sessionAttributes != null) {
                    sessionAttributes.put("email", user.getEmail());
                }
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                ensureAuthenticated(accessor);
                propagateAuthToSecurityContext(accessor);
                authorizeSubscription(accessor);
            }

            if (StompCommand.SEND.equals(accessor.getCommand())) {
                ensureAuthenticated(accessor);
                propagateAuthToSecurityContext(accessor);
                authorizeSend(accessor);
            }
        }

        return message;
    }

    private User resolveUserFromConnect(StompHeaderAccessor accessor) {
        String rawAuthorization = accessor.getFirstNativeHeader("Authorization");
        if (rawAuthorization == null || rawAuthorization.isBlank()) {
            rawAuthorization = accessor.getFirstNativeHeader("authorization");
        }

        String token = extractBearerToken(rawAuthorization);
        try {
            String tokenType = jwtUtil.extractTokenType(token);
            if (!"access".equals(tokenType)) {
                throw new UnauthorizedException("Invalid websocket token type");
            }

            String email = jwtUtil.extractUsername(token);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid websocket token");
        }
    }

    private User resolveUser(StompHeaderAccessor accessor) {
        if (accessor.getUser() != null && accessor.getUser().getName() != null) {
            String emailFromPrincipal = accessor.getUser().getName();
            return userRepository.findByEmail(emailFromPrincipal)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));
        }

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) throw new UnauthorizedException("No session attributes");

        String email = (String) sessionAttributes.get("email");
        if (email == null || email.isBlank()) throw new UnauthorizedException("Missing email in session");

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedException("Missing websocket Authorization header");
        }

        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException("Invalid websocket Authorization format");
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new UnauthorizedException("Missing websocket token");
        }
        return token;
    }

    private UsernamePasswordAuthenticationToken buildAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    private void propagateAuthToSecurityContext(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth) {
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    private void ensureAuthenticated(StompHeaderAccessor accessor) {
        if (accessor.getUser() != null) {
            return;
        }
        User user = resolveUser(accessor);
        accessor.setUser(buildAuthentication(user));
    }

    private void authorizeSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || destination.isBlank()) {
            throw new ForbiddenException("Invalid send destination");
        }

        if (destination.equals("/app/chat.send")
                || destination.equals("/app/chat.typing")
                || destination.equals("/app/notify.read")) {
            return;
        }

        throw new ForbiddenException("You are not allowed to send to this destination");
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || destination.isBlank()) return;

        String normalizedDestination = destination.endsWith(".typing")
                ? destination.substring(0, destination.length() - ".typing".length())
                : destination;

        if (normalizedDestination.startsWith("/user/queue/")) {
            return;
        }

        if (!normalizedDestination.startsWith("/topic/room.")) {
            throw new ForbiddenException("You are not allowed to subscribe to this destination");
        }

        String roomIdText = normalizedDestination.substring("/topic/room.".length());
        UUID roomId;
        try {
            roomId = UUID.fromString(roomIdText);
        } catch (IllegalArgumentException ex) {
            throw new ForbiddenException("Invalid chat subscription destination");
        }

        User user = resolveUser(accessor);
        if (user.getRole() == Role.ADMIN) {
            throw new ForbiddenException("Admin account is not part of chat channels");
        }

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ForbiddenException("Chat room not found for subscription"));

        if (room.getType() == ChatRoomType.GENERAL) {
            UUID roomOfficeId = room.getOffice() != null ? room.getOffice().getId() : null;
            UUID userOfficeId = user.getOffice() != null ? user.getOffice().getId() : null;
            if (roomOfficeId == null || !roomOfficeId.equals(userOfficeId)) {
                throw new ForbiddenException("You are not allowed to subscribe to this room");
            }
            return;
        }

        if (room.getType() != ChatRoomType.DEPARTMENT) {
            throw new ForbiddenException("You are not allowed to subscribe to this room");
        }

        UUID roomDepartmentId = room.getDepartment() != null ? room.getDepartment().getId() : null;
        UUID userDepartmentId = user.getDepartment() != null ? user.getDepartment().getId() : null;
        if (userDepartmentId == null || !userDepartmentId.equals(roomDepartmentId)) {
            throw new ForbiddenException("You are not allowed to subscribe to this room");
        }
    }
}