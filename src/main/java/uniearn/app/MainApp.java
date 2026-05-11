package uniearn.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uniearn.server.security.SecurityCallbackServer;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/application/LoadingView.fxml"));

        Scene scene = new Scene(root);

        // Keep the main window maximized whenever a new Scene is set.
        primaryStage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    primaryStage.setMinWidth(900);
                    primaryStage.setMinHeight(600);
                    primaryStage.setMaximized(true);
                });
            }
        });

        primaryStage.setTitle("UniEarn — Plateforme de freelancing étudiant");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setMaximized(true);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    @Override
    public void stop() throws Exception {
        SecurityCallbackServer.stop();
        super.stop();
    }
<<<<<<< HEAD
=======


>>>>>>> b3914dda8101150b70e536030bc2eafc2f5a95fa

    public static void main(String[] args) {
        launch(args);
    }
}
