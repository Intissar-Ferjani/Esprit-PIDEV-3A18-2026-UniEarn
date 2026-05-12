package uniearn.server.forum;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import uniearn.model.entities.forum.ChatMessage;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handles public chat messages.
     * Sent to /app/chat.sendMessage
     * Broadcast to /topic/public
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }

    /**
     * Handles adding a user (e.g., for system announcements).
     * Sent to /app/chat.addUser
     * Broadcast to /topic/public
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage) {
        // Add username in web socket session logic if needed
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        return chatMessage;
    }

    /**
     * Handles private messages between users.
     * Sent to /app/chat.privateMessage
     * Forwarded to both the recipient and sender's private queues.
     */
    @MessageMapping("/chat.privateMessage")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage) {
        if (chatMessage.getRecipient() != null && !chatMessage.getRecipient().isEmpty()) {
            // Send to recipient
            messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipient(), "/queue/messages", chatMessage);
            // Also send back to sender so they see their own message
            messagingTemplate.convertAndSendToUser(
                chatMessage.getSender(), "/queue/messages", chatMessage);
        }
    }

}
