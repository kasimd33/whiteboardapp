package com.antigravity.board.config;

import com.antigravity.board.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketSecurityConfig implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = getTokenFromHeaders(accessor);

            if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
                String username = tokenProvider.getUsernameFromToken(token);
                UUID userId = tokenProvider.getUserIdFromToken(token);
                String role = tokenProvider.getRoleFromToken(token);
                String authority = role != null ? "ROLE_" + role : "ROLE_PARTICIPANT";

                com.antigravity.board.security.UserPrincipal principal =
                        new com.antigravity.board.security.UserPrincipal(
                                userId, username, List.of(new SimpleGrantedAuthority(authority)));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

                accessor.setUser(authentication);
            }
        }

        return message;
    }

    private String getTokenFromHeaders(StompHeaderAccessor accessor) {
        List<String> authHeader = accessor.getNativeHeader("Authorization");
        if (authHeader != null && !authHeader.isEmpty()) {
            String bearer = authHeader.get(0);
            if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
                return bearer.substring(7);
            }
        }
        return null;
    }
}
