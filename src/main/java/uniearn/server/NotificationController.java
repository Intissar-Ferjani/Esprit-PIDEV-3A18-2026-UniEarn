package uniearn.server;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import uniearn.model.dto.NotificationMsg;

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
        // Send to specific user
        // The client subscribes to /user/queue/notifications
        // Server sends to /user/{recipientId}/queue/notifications
        // Note: Spring's convertAndSendToUser adds the /user/ prefix automatically.
        // So we send to destination "/queue/notifications".

        messagingTemplate.convertAndSendToUser(
                notification.getRecipientId(),
                "/queue/notifications",
                notification);

        // Also broadcast to public if needed, but notifications are usually private.
    }
}
