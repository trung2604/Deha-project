package com.deha.HumanResourceManagement.config.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Handles uncaught exceptions in WebSocket message handlers
 * to prevent "AuthenticationCredentialsNotFoundException" and other exceptions
 * from breaking the WebSocket connection.
 */
@Component
public class WebSocketExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketExceptionHandler.class);

    /**
     * Catch-all handler for exceptions that occur during WebSocket message processing.
     * This prevents exceptions from breaking the WebSocket session.
     */
    public void handleWebSocketException(Exception ex, Message<?> message) {
        logger.warn("WebSocket message processing error: {}", ex.getMessage(), ex);
        // Silently handle - do not rethrow as it breaks the session
    }
}
