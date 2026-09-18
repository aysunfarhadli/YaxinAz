package com.yaxinaz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;

/**
 * Real-time notification push (spec section 56). Intentionally simple: the STOMP handshake is
 * public (matches SecurityConfig's "/ws/**" permitAll from Phase 3) and clients subscribe to a
 * per-user topic keyed by their own user id
 * ("/topic/notifications/{userId}") rather than layering STOMP-level JWT
 * authentication on top - section 56 explicitly asks to "keep implementation understandable" over
 * building a fully hardened WS auth layer for a course project. The REST API remains the source of
 * truth (GET /api/notifications); the socket is a best-effort live nudge, not the only way to learn
 * about a new notification.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
