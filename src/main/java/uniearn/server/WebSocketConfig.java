package uniearn.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to carry the greeting messages
        // back to the client on destinations prefixed with "/topic" and "/queue"
        config.enableSimpleBroker("/topic", "/queue", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registers the "/ws" endpoint, enabling the socketJS protocol.
        // Handshake endpoint: ws://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new org.springframework.web.socket.server.support.DefaultHandshakeHandler() {
                    @Override
                    protected java.security.Principal determineUser(
                            org.springframework.http.server.ServerHttpRequest request,
                            org.springframework.web.socket.WebSocketHandler wsHandler,
                            java.util.Map<String, Object> attributes) {
                        // Extract user from query param: ws://localhost:8080/ws?user=username
                        if (request instanceof org.springframework.http.server.ServletServerHttpRequest) {
                            org.springframework.http.server.ServletServerHttpRequest servletRequest = (org.springframework.http.server.ServletServerHttpRequest) request;
                            String user = servletRequest.getServletRequest().getParameter("user");
                            if (user != null) {
                                return new java.security.Principal() {
                                    @Override
                                    public String getName() {
                                        return user;
                                    }
                                };
                            }
                        }
                        return null;
                    }
                })
                .setAllowedOrigins("*");
    }
}
