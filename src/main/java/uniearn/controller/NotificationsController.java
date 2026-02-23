package uniearn.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.application.Platform;
import java.io.IOException;
import uniearn.model.dto.NotificationMsg;
import uniearn.services.WebSocketService;

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

        // Connect if not connected
        String username = "Forum User";
        if (!WebSocketService.getInstance().isConnected()) {
            WebSocketService.getInstance().connect(username);
        }

        // Subscribe to real-time notifications
        WebSocketService.getInstance().subscribe("/topic/notifications", NotificationMsg.class, notification -> {
            Platform.runLater(() -> {
                addNotificationToUI(notification);
            });
        });
    }

    private void addNotificationToUI(NotificationMsg notification) {
        if (notificationsContainer == null)
            return;

        javafx.scene.layout.HBox notifRow = new javafx.scene.layout.HBox(10);
        notifRow.setStyle("-fx-background-color: #eaf6ff; -fx-padding: 15; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        notifRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Icon placeholder
        javafx.scene.layout.StackPane iconPane = new javafx.scene.layout.StackPane();
        iconPane.setPrefSize(40, 40);
        iconPane.setStyle("-fx-background-color: #3498db; -fx-background-radius: 20;");
        Label iconLabel = new Label("🔔");
        iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        iconPane.getChildren().add(iconLabel);

        VBox contentBox = new VBox(2);
        Label titleLabel = new Label(notification.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        Label msgLabel = new Label(notification.getMessage());
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

        contentBox.getChildren().addAll(titleLabel, msgLabel);

        // Unread indicator
        javafx.scene.shape.Circle unreadDot = new javafx.scene.shape.Circle(4, javafx.scene.paint.Color.web("#3498db"));

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        notifRow.getChildren().addAll(iconPane, contentBox, spacer, unreadDot);

        // Add to top
        notificationsContainer.getChildren().add(0, notifRow);
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
