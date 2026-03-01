package uniearn.controller.profile.forum;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import uniearn.model.entities.forum.NotificationMsg;
import uniearn.services.forum.NotificationStore;
import uniearn.services.forum.WebSocketService;

public class NotificationsController {

    @FXML
    private Button backButton;
    @FXML
    private VBox notificationsContainer;

    @FXML
    public void initialize() {
        // Clear dummy data from FXML
        if (notificationsContainer != null)
            notificationsContainer.getChildren().clear();

        // Load all stored notifications first
        for (NotificationMsg stored : NotificationStore.getInstance().getAll()) {
            addNotificationToUI(stored);
        }

        // If no notifications yet, show a placeholder
        if (NotificationStore.getInstance().size() == 0) {
            Label emptyLabel = new Label("No notifications yet");
            emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px; -fx-padding: 20;");
            notificationsContainer.getChildren().add(emptyLabel);
        }

        // Subscribe to live notifications so new ones appear in real-time
        if (WebSocketService.getInstance().isConnected()) {
            WebSocketService.getInstance().subscribe("/topic/notifications", NotificationMsg.class, notification -> {
                Platform.runLater(() -> {
                    // Remove empty placeholder if present
                    notificationsContainer.getChildren().removeIf(node ->
                            node instanceof Label && ((Label) node).getText().equals("No notifications yet"));
                    addNotificationToUI(notification);
                });
            });
        }
    }

    private void addNotificationToUI(NotificationMsg notification) {
        if (notificationsContainer == null)
            return;

        javafx.scene.layout.HBox notifRow = new javafx.scene.layout.HBox(12);
        notifRow.setStyle("-fx-background-color: #eaf6ff; -fx-padding: 15; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        notifRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Choose icon and color based on notification type
        String icon = "🔔";
        String iconBg = "#3498db";
        String type = notification.getType() != null ? notification.getType() : "";
        switch (type) {
            case "REACTION":
                String msg = notification.getMessage() != null ? notification.getMessage() : "";
                if (msg.contains("disliked")) {
                    icon = "👎";
                    iconBg = "#e74c3c";
                } else {
                    icon = "👍";
                    iconBg = "#1a56db";
                }
                break;
            case "COMMENT":
                icon = "💬";
                iconBg = "#2ecc71";
                break;
            case "MESSAGE":
                icon = "✉";
                iconBg = "#9b59b6";
                break;
            default:
                icon = "🔔";
                iconBg = "#3498db";
                break;
        }

        javafx.scene.layout.StackPane iconPane = new javafx.scene.layout.StackPane();
        iconPane.setPrefSize(40, 40);
        iconPane.setMinSize(40, 40);
        iconPane.setStyle("-fx-background-color: " + iconBg + "; -fx-background-radius: 20;");
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        iconPane.getChildren().add(iconLabel);

        VBox contentBox = new VBox(2);
        // Show sender name
        String senderText = notification.getFromUser() != null ? notification.getFromUser() : "Someone";
        Label senderLabel = new Label(senderText);
        senderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        // Show notification message
        String messageText = notification.getMessage() != null ? notification.getMessage() : "";
        Label msgLabel = new Label(messageText);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

        contentBox.getChildren().addAll(senderLabel, msgLabel);

        // Unread dot
        javafx.scene.shape.Circle unreadDot = new javafx.scene.shape.Circle(4, javafx.scene.paint.Color.web(iconBg));

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        notifRow.getChildren().addAll(iconPane, contentBox, spacer, unreadDot);

        // Add to top
        notificationsContainer.getChildren().add(0, notifRow);
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/Forum.fxml"));
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
    private void markAllRead() {
        // Change all unread notification backgrounds to white (read state)
        if (notificationsContainer != null) {
            notificationsContainer.getChildren().forEach(node -> {
                if (node instanceof javafx.scene.layout.HBox) {
                    String style = node.getStyle();
                    if (style != null && style.contains("#eaf6ff")) {
                        node.setStyle(style.replace("#eaf6ff", "white")
                                .replace("#d4edfc", "#e0e0e0"));
                        // Remove the blue unread indicator dot
                        javafx.scene.layout.HBox hbox = (javafx.scene.layout.HBox) node;
                        hbox.getChildren().removeIf(child -> child instanceof javafx.scene.layout.Region &&
                                child.getStyle() != null &&
                                child.getStyle().contains("#3498db"));
                    }
                }
            });
        }
        showAlert("Success", "All notifications marked as read!");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
