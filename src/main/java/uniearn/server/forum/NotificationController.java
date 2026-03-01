package uniearn.server.forum;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import uniearn.model.entities.forum.NotificationMsg;

@Controller
public class NotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handles notifications (likes, comments).
     * Sent to /app/notification
     * Forwarded to /user/{recipientId}/queue/notifications
     */
    @MessageMapping("/notification")
    public void sendNotification(@Payload NotificationMsg notification) {
        // Broadcast to /topic/notifications so all connected clients receive it.
        // Each client filters by recipientId to show only relevant notifications.
        messagingTemplate.convertAndSend("/topic/notifications", notification);

        // Also send to specific user if recipientId is set
        if (notification.getRecipientId() != null && !notification.getRecipientId().isEmpty()) {
            messagingTemplate.convertAndSendToUser(
                    notification.getRecipientId(),
                    "/queue/notifications",
                    notification);
        }
    }
}
