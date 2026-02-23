package uniearn.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;

import uniearn.services.WebSocketService;
import uniearn.model.dto.ChatMessage;
import uniearn.model.dto.NotificationMsg;

import java.io.IOException;

public class MessagesController {

    @FXML
    private Button backButton;
    @FXML
    private VBox conversationList;
    @FXML
    private VBox chatMessages;
    @FXML
    private TextField messageInput;
    @FXML
    private TextField searchField;

    @FXML
    public void initialize() {
        // Clear dummy data from FXML
        if (conversationList != null)
            conversationList.getChildren().clear();
        if (chatMessages != null)
            chatMessages.getChildren().clear();

        // Connect to WebSocket (if not already connected)
        String username = "Forum User";
        if (!WebSocketService.getInstance().isConnected()) {
            WebSocketService.getInstance().connect(username);
        }

        // Subscribe to public chat
        WebSocketService.getInstance().subscribe("/topic/public", ChatMessage.class, message -> {
            Platform.runLater(() -> {
                addMessageToChat(message.getContent(), message.getSender(), false);
            });
        });
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/Freelancer/Forum.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Freelancer Forum");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load Forum page!");
        }
    }

    @FXML
    private void sendMessage() {
        if (messageInput != null) {
            String text = messageInput.getText().trim();
            if (!text.isEmpty()) {
                String username = "Forum User";

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setSender(username);
                chatMessage.setContent(text);
                chatMessage.setType(ChatMessage.MessageType.CHAT);
                WebSocketService.getInstance().send("/app/chat.sendMessage", chatMessage);

                // Send real-time notification so others see "X sent a message"
                if (WebSocketService.getInstance().isConnected()) {
                    NotificationMsg notif = new NotificationMsg();
                    notif.setFromUser(username);
                    notif.setTitle("New Message");
                    notif.setMessage("sent a message in chat");
                    notif.setType("MESSAGE");
                    WebSocketService.getInstance().send("/app/notification", notif);
                }

                messageInput.clear();
            }
        }
    }

    private void addMessageToChat(String text, String sender, boolean isSelf) {
        if (chatMessages == null)
            return;

        // Check if sender is me
        String myName = "Forum User";
        boolean isMe = sender.equals(myName) || isSelf;

        javafx.scene.layout.HBox msgRow = new javafx.scene.layout.HBox();
        msgRow.setAlignment(isMe ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);

        VBox msgBubble = new VBox();
        msgBubble.setMaxWidth(400);
        msgBubble.setStyle(isMe
                ? "-fx-background-color: #3498db; -fx-padding: 12 16 12 16; -fx-background-radius: 15 15 3 15;"
                : "-fx-background-color: #e0e0e0; -fx-padding: 12 16 12 16; -fx-background-radius: 15 15 15 3;");

        Label msgLabel = new Label(text);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (isMe ? "white" : "black") + ";");

        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (isMe ? "#bde0ff" : "#7f8c8d") + ";");

        msgBubble.getChildren().addAll(msgLabel, senderLabel);
        msgRow.getChildren().add(msgBubble);

        chatMessages.getChildren().add(msgRow);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
