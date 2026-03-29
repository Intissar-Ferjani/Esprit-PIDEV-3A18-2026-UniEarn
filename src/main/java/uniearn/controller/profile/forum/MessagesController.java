package uniearn.controller.profile.forum;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import uniearn.database.SessionManager;
import uniearn.model.entities.forum.ChatMessage;
import uniearn.model.entities.forum.NotificationMsg;
import uniearn.services.forum.PrivateMessageService;
import uniearn.services.forum.WebSocketService;

public class MessagesController {

    @FXML private Button backButton;
    @FXML private VBox conversationList;
    @FXML private VBox chatMessages;
    @FXML private TextField messageInput;
    @FXML private TextField searchField;
    @FXML private Label chatHeaderName;
    @FXML private Label chatHeaderStatus;
    @FXML private Label chatHeaderInitial;

    private final PrivateMessageService messageService = new PrivateMessageService();
    private String currentUserName = "Guest";
    private String activeChatUser = null;

    /** Call this after loading FXML but before showing the scene to pre-select a conversation. */
    public void setChatWith(String username) {
        this.activeChatUser = username;
    }

    @FXML
    public void initialize() {
        if (SessionManager.getInstance().isLoggedIn()) {
            currentUserName = SessionManager.getInstance().getCurrentUserName();
        }

        if (conversationList != null) conversationList.getChildren().clear();
        if (chatMessages != null) chatMessages.getChildren().clear();

        loadConversationList();

        if (activeChatUser != null && !activeChatUser.isEmpty()) {
            openConversation(activeChatUser);
        } else {
            showNoChatSelected();
        }

        // Connect to WebSocket (with retry) and subscribe for real-time messages
        new Thread(() -> {
            // Ensure WebSocket is connected (retry up to 5 times)
            for (int attempt = 1; attempt <= 5; attempt++) {
                if (WebSocketService.getInstance().isConnected()) break;
                System.out.println("Messages: WebSocket connect attempt " + attempt + "...");
                WebSocketService.getInstance().connect(currentUserName);
                if (WebSocketService.getInstance().isConnected()) break;
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }

            if (WebSocketService.getInstance().isConnected()) {
                System.out.println("✅ Messages: WebSocket connected! Subscribing to private messages...");
                String topic = "/user/" + encodeUser(currentUserName) + "/queue/messages";
                // Use forceSubscribe to replace any stale callback from previous page visits
                WebSocketService.getInstance().forceSubscribe(topic, ChatMessage.class, message ->
                    Platform.runLater(() -> {
                        // If we're chatting with the sender, add the bubble immediately
                        if (activeChatUser != null &&
                            (message.getSender().equals(activeChatUser) || message.getRecipient().equals(activeChatUser))) {
                            if (!message.getSender().equals(currentUserName)) {
                                addMessageBubble(message.getContent(), message.getSender(), false, message.getTimestamp());
                                scrollChatToBottom();
                            }
                        }
                        // Always refresh the conversation list so new messages show up
                        loadConversationList();
                    }));
            } else {
                System.err.println("⚠ Messages: Could not connect to WebSocket. Real-time delivery disabled.");
            }
        }).start();
    }

    /** Scroll the chat messages area to the bottom to show the latest message. */
    private void scrollChatToBottom() {
        if (chatMessages != null && chatMessages.getParent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) chatMessages.getParent();
            // Use runLater to ensure layout is done before scrolling
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
        }
    }

    private void loadConversationList() {
        if (conversationList == null) return;
        conversationList.getChildren().clear();

        Map<String, String> conversations = messageService.getConversationList(currentUserName);

        // Also add project-linked contacts (clients ↔ freelancers sharing projects)
        Map<String, String> projectContacts = messageService.getProjectLinkedContacts(currentUserName);
        for (Map.Entry<String, String> entry : projectContacts.entrySet()) {
            if (!conversations.containsKey(entry.getKey())) {
                conversations.put(entry.getKey(), entry.getValue());
            }
        }

        // If target user has no existing conversation yet, add them at top
        if (activeChatUser != null && !conversations.containsKey(activeChatUser)) {
            conversationList.getChildren().add(
                createConversationItem(activeChatUser, "Start a conversation...", true));
        }

        for (Map.Entry<String, String> entry : conversations.entrySet()) {
            String otherUser = entry.getKey();
            String lastMsg = entry.getValue();
            boolean isActive = otherUser.equals(activeChatUser);
            conversationList.getChildren().add(createConversationItem(otherUser, lastMsg, isActive));
        }

        // Add all other freelancers as available contacts
        java.util.List<String> freelancerNames = messageService.getAllFreelancerNames(currentUserName);
        java.util.List<String> newFreelancers = new java.util.ArrayList<>();
        for (String name : freelancerNames) {
            if (!conversations.containsKey(name) &&
                (activeChatUser == null || !activeChatUser.equals(name))) {
                newFreelancers.add(name);
            }
        }
        if (!newFreelancers.isEmpty()) {
            // Section header
            Label header = new Label("💼 Other Freelancers");
            header.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d; " +
                "-fx-padding: 10 15 5 15; -fx-background-color: #f8f9fa;");
            header.setMaxWidth(Double.MAX_VALUE);
            conversationList.getChildren().add(header);

            for (String name : newFreelancers) {
                boolean isActive = name.equals(activeChatUser);
                conversationList.getChildren().add(
                    createConversationItem(name, "💬 Freelancer — tap to chat", isActive));
            }
        }
    }

    private HBox createConversationItem(String username, String lastMessage, boolean isActive) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 15, 12, 15));
        String activeStyle = "-fx-background-color: #e3f2fd; -fx-cursor: hand; -fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0;";
        String normalStyle = "-fx-background-color: white; -fx-cursor: hand; -fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0;";
        item.setStyle(isActive ? activeStyle : normalStyle);

        String initial = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();
        String[] colors = {"#3498db", "#e74c3c", "#2ecc71", "#9b59b6", "#f39c12", "#1abc9c"};
        String color = colors[Math.abs(username.hashCode()) % colors.length];

        Label avatarLabel = new Label(initial);
        avatarLabel.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
            "-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 14 10 14; -fx-background-radius: 50;");

        VBox textBox = new VBox(3);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label nameLabel = new Label(username);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label previewLabel = new Label(lastMessage.length() > 35 ? lastMessage.substring(0, 35) + "..." : lastMessage);
        previewLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        textBox.getChildren().addAll(nameLabel, previewLabel);
        item.getChildren().addAll(avatarLabel, textBox);

        item.setOnMouseClicked(e -> {
            activeChatUser = username;
            openConversation(username);
            loadConversationList();
        });
        item.setOnMouseEntered(e -> {
            if (!username.equals(activeChatUser)) item.setStyle("-fx-background-color: #f5f5f5; -fx-cursor: hand; -fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0;");
        });
        item.setOnMouseExited(e -> {
            item.setStyle(username.equals(activeChatUser) ? activeStyle : normalStyle);
        });
        return item;
    }

    private void openConversation(String otherUser) {
        if (chatMessages == null) return;
        chatMessages.getChildren().clear();

        if (chatHeaderName != null) chatHeaderName.setText(otherUser);
        if (chatHeaderStatus != null) chatHeaderStatus.setText("\u25CF Online");
        if (chatHeaderInitial != null)
            chatHeaderInitial.setText(otherUser.isEmpty() ? "?" : otherUser.substring(0, 1).toUpperCase());

        List<ChatMessage> history = messageService.getConversation(currentUserName, otherUser);
        for (ChatMessage msg : history) {
            addMessageBubble(msg.getContent(), msg.getSender(), msg.getSender().equals(currentUserName), msg.getTimestamp());
        }
        scrollChatToBottom();
    }

    private void showNoChatSelected() {
        if (chatMessages == null) return;
        chatMessages.getChildren().clear();
        Label placeholder = new Label("Select a conversation or click on a post author to start chatting");
        placeholder.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px; -fx-padding: 40;");
        placeholder.setWrapText(true);
        chatMessages.getChildren().add(placeholder);
    }

    @FXML
    private void sendMessage() {
        if (messageInput == null || activeChatUser == null) return;
        String text = messageInput.getText().trim();
        if (text.isEmpty()) return;

        messageService.saveMessage(currentUserName, activeChatUser, text);
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        addMessageBubble(text, currentUserName, true, time);

        if (WebSocketService.getInstance().isConnected()) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSender(currentUserName);
            chatMessage.setRecipient(activeChatUser);
            chatMessage.setContent(text);
            chatMessage.setTimestamp(time);
            chatMessage.setType(ChatMessage.MessageType.CHAT);
            WebSocketService.getInstance().send("/app/chat.privateMessage", chatMessage);

            NotificationMsg notif = new NotificationMsg();
            notif.setFromUser(currentUserName);
            notif.setTitle("New Message");
            notif.setMessage("sent you a message: " + (text.length() > 40 ? text.substring(0, 40) + "..." : text));
            notif.setRecipientId(activeChatUser);
            notif.setType("MESSAGE");
            WebSocketService.getInstance().send("/app/notification", notif);
        }

        messageInput.clear();
        scrollChatToBottom();
        loadConversationList();
    }

    private void addMessageBubble(String text, String sender, boolean isSelf, String time) {
        if (chatMessages == null) return;

        HBox row = new HBox();
        row.setAlignment(isSelf ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 10, 2, 10));

        VBox bubble = new VBox(4);
        bubble.setMaxWidth(400);
        bubble.setStyle(isSelf
            ? "-fx-background-color: #3498db; -fx-padding: 12 16 12 16; -fx-background-radius: 15 15 3 15;"
            : "-fx-background-color: #e8e8e8; -fx-padding: 12 16 12 16; -fx-background-radius: 15 15 15 3;");

        if (!isSelf) {
            Label senderLabel = new Label(sender);
            senderLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            bubble.getChildren().add(senderLabel);
        }

        Label msgLabel = new Label(text);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (isSelf ? "white" : "#333") + ";");

        Label timeLabel = new Label(time != null ? time : "");
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (isSelf ? "#bde0ff" : "#95a5a6") + ";");

        bubble.getChildren().addAll(msgLabel, timeLabel);
        row.getChildren().add(bubble);
        chatMessages.getChildren().add(row);
    }

    @FXML
    private void goBack() {
        try {
            uniearn.model.entities.users.User currentUser = SessionManager.getInstance().getCurrentUser();
            Parent root;
            if (currentUser != null && currentUser.getRole() == uniearn.model.enums.UserRole.CLIENT) {
                // Client user — go back to client profile
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/client-profile.fxml"));
                root = loader.load();
                uniearn.services.users.client.ClientService cs = new uniearn.services.users.client.ClientService();
                uniearn.model.entities.users.client.Client client = cs.getClientById(currentUser.getIdUser());
                if (client != null) {
                    uniearn.controller.profile.client.ClientProfileController ctrl = loader.getController();
                    ctrl.setClientData(client);
                }
            } else {
                // Freelancer user — go back to freelancer profile
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/freelancer-profile.fxml"));
                root = loader.load();

                // Pass freelancer data back to dashboard
                uniearn.controller.profile.freelancer.FreelancerProfileController controller = loader.getController();
                if (SessionManager.getInstance().isLoggedIn()) {
                    int userId = SessionManager.getInstance().getCurrentUserId();
                    uniearn.services.users.freelancer.FreelancerService freelancerService = new uniearn.services.users.freelancer.FreelancerService();
                    uniearn.model.entities.users.freelancer.Freelancer freelancer = freelancerService.getFreelancerById(userId);
                    if (freelancer != null) {
                        controller.setFreelancerData(freelancer);
                    }
                }
            }

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard - UniEarn");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load Dashboard page!");
        }
    }

    private String encodeUser(String name) {
        try {
            return java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return name;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
