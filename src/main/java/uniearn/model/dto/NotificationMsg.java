package uniearn.model.dto;

public class NotificationMsg {
    private String title;
    private String message;
    private String recipientId; // Can be username or userId
    private String type; // e.g., "LIKE", "COMMENT", "MESSAGE"

    public NotificationMsg() {
    }

    public NotificationMsg(String title, String message, String recipientId, String type) {
        this.title = title;
        this.message = message;
        this.recipientId = recipientId;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
