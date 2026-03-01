package uniearn.app;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uniearn.server.forum.ServerApp;

public class forumApp extends Application {
    private static ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Start the Spring Boot WebSocket server in a background thread
        new Thread(() -> {
            try {
                SpringApplication app = new SpringApplication(ServerApp.class);
                app.setDefaultProperties(Collections.singletonMap("server.port", "8081"));
                springContext = app.run();
                System.out.println("✅ WebSocket server started on port 8081");
            } catch (Exception e) {
                System.err.println("⚠ Could not start WebSocket server: " + e.getMessage());
            }
        }, "spring-boot-server").start();

        // Give the server a moment to start
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/profile/freelancer/Forum.fxml"));
        primaryStage.setTitle("Freelancer Forum");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Shut down the Spring Boot server when the JavaFX app closes
        if (springContext != null) {
            springContext.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
