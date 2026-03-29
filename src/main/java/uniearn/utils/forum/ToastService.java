package uniearn.utils.forum;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ToastService {

    /**
     * Shows a brief, styled notification popup at the bottom center of the current stage.
     */
    public static void showToast(Stage stage, String message, String type) {
        if (stage == null || message == null) return;

        Platform.runLater(() -> {
            HBox toast = new HBox(12);
            toast.setAlignment(Pos.CENTER_LEFT);
            toast.setPadding(new Insets(10, 20, 10, 20));
            toast.setMaxWidth(400);
            
            // Choose color based on type
            String bgColor = "#34495e"; // default dark
            String icon = "🔔";
            
            if (type != null) {
                switch (type.toUpperCase()) {
                    case "REACTION":
                        bgColor = message.contains("disl") ? "#e74c3c" : "#1a56db";
                        icon = message.contains("disl") ? "👎" : "👍";
                        break;
                    case "COMMENT":
                        bgColor = "#2ecc71";
                        icon = "💬";
                        break;
                    case "SUCCESS":
                        bgColor = "#27ae60";
                        icon = "✅";
                        break;
                    case "ERROR":
                        bgColor = "#c0392b";
                        icon = "❌";
                        break;
                }
            }

            toast.setStyle("-fx-background-color: " + bgColor + "; " +
                    "-fx-background-radius: 25; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);");

            Label iconLabel = new Label(icon);
            iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            
            Label msgLabel = new Label(message);
            msgLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
            msgLabel.setWrapText(true);

            toast.getChildren().addAll(iconLabel, msgLabel);

            // Get the root StackPane if possible, or add to the scene's root
            Scene scene = stage.getScene();
            if (scene != null && scene.getRoot() instanceof StackPane) {
                StackPane root = (StackPane) scene.getRoot();
                root.getChildren().add(toast);
                StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
                StackPane.setMargin(toast, new Insets(0, 0, 50, 0));

                // Animation
                toast.setOpacity(0);
                toast.setTranslateY(20);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                TranslateTransition moveUp = new TranslateTransition(Duration.millis(300), toast);
                moveUp.setFromY(20);
                moveUp.setToY(0);

                fadeIn.play();
                moveUp.play();

                PauseTransition pause = new PauseTransition(Duration.seconds(3));
                pause.setOnFinished(e -> {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(500), toast);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);
                    fadeOut.setOnFinished(ev -> root.getChildren().remove(toast));
                    fadeOut.play();
                });
                pause.play();
            } else {
                // Fallback for non-StackPane roots (less ideal but functional)
                // In a production app, we might create a separate transparent Stage for toasts
                System.out.println("Toast: " + message);
            }
        });
    }
}
