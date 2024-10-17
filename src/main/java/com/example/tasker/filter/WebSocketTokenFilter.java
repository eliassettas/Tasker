package com.example.tasker.filter;

import java.time.Instant;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketHttpHeaders;

import com.example.tasker.service.UserDetailsServiceImpl;

@Component
public class WebSocketTokenFilter implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final UserDetailsServiceImpl userDetailsService;

    public WebSocketTokenFilter(
            JwtDecoder jwtDecoder,
            UserDetailsServiceImpl userDetailsService
    ) {
        this.jwtDecoder = jwtDecoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    @Transactional
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            throw new AccessDeniedException("Failed to retrieve message headers");
        }

        if (accessor.getCommand() == StompCommand.CONNECT) {
            authenticate(accessor);
        }
        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader(WebSocketHttpHeaders.AUTHORIZATION);
        if (token == null) {
            throw new AccessDeniedException("No token provided");
        }

        Jwt jwt = jwtDecoder.decode(token.substring(7));
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null || expiresAt.isBefore(Instant.now())) {
            throw new AccessDeniedException("Token has expired");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(jwt.getSubject());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        accessor.setUser(authentication);
    }
}
