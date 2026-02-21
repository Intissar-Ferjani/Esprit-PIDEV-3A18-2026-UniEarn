package uniearn.services;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.function.Consumer;

public class WebSocketService {

    private static WebSocketService instance;
    private StompSession session;
    private String username;

    private WebSocketService() {
    }

    public static synchronized WebSocketService getInstance() {
        if (instance == null) {
            instance = new WebSocketService();
        }
        return instance;
    }

    public void connect(String username) {
        if (isConnected()) {
            return;
        }
        this.username = username;
        try {
            // Explicitly use Tyrus ClientManager to avoid Tomcat WebSocket container issues
            // This fixes "Handshake error" and "AuthenticationException"
            org.glassfish.tyrus.client.ClientManager clientManager = org.glassfish.tyrus.client.ClientManager
                    .createClient();
            jakarta.websocket.WebSocketContainer container = clientManager;
            StandardWebSocketClient client = new StandardWebSocketClient(container);

            WebSocketStompClient stompClient = new WebSocketStompClient(client);
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            // URL Encode username to handle spaces (fixes "Handshake error" 400 Bad
            // Request)
            String encodedUsername = java.net.URLEncoder.encode(username, java.nio.charset.StandardCharsets.UTF_8);
            String url = "ws://localhost:8081/ws?user=" + encodedUsername;

            session = stompClient.connectAsync(url, new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    System.out.println("✅ Connected to WebSocket successfully!");
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    System.out.println("❌ Transport error: " + exception.getMessage());
                }
            }).get();
        } catch (Exception e) {
            System.err.println("❌ WebSocket connection failed: " + e.getMessage());
            // Do not throw runtime exception to avoid crashing the UI
            e.printStackTrace();
        }
    }

    public <T> void subscribe(String topic, Class<T> payloadType, Consumer<T> callback) {
        if (isConnected()) {
            session.subscribe(topic, new StompFrameHandler() {

                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return payloadType;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    callback.accept(payloadType.cast(payload));
                }
            });
        }
    }

    public void send(String destination, Object payload) {
        if (isConnected()) {
            session.send(destination, payload);
        }
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }
}
