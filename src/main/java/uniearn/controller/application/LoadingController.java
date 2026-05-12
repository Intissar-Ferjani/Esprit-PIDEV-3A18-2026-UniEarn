package uniearn.controller.application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class LoadingController {

    @FXML
    private StackPane rootPane;

    @FXML
    private MediaView mediaView;

    @FXML
    private Label lblLoading;

    private MediaPlayer mediaPlayer;

    @FXML
    public void initialize() {
        System.out.println("LoadingController initialized");
        playVideo();
    }

    private void playVideo() {
        try {
            // Try to find the video in resources
            URL videoUrl = getClass().getResource("/images/uniearn_loading.mp4");

            if (videoUrl == null) {
                System.err.println("❌ Video file not found: /images/loading_video.mp4");
                lblLoading.setVisible(true);
                lblLoading.setText("Video not found. Loading application...");

                // Fallback: Transition after a short delay
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(2));
                pause.setOnFinished(e -> transitionToHome());
                pause.play();
                return;
            }

            Media media = new Media(videoUrl.toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            // Transition to Home when video finishes
            mediaPlayer.setOnEndOfMedia(this::transitionToHome);

            // Also handle errors
            mediaPlayer.setOnError(() -> {
                System.err.println("❌ Media player error: " + mediaPlayer.getError().getMessage());
                transitionToHome();
            });

            mediaPlayer.play();
            System.out.println("✓ Video playback started");

        } catch (Exception e) {
            System.err.println("❌ Error playing video: " + e.getMessage());
            e.printStackTrace();
            transitionToHome();
        }
    }

    private void transitionToHome() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        try {
            System.out.println("Transitioning to HomePage...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home/HomePage.fxml"));
            Parent homeRoot = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(homeRoot, 1200, 700);

            stage.setScene(scene);
            stage.centerOnScreen();

            System.out.println("✓ Navigation to HomePage complete");

        } catch (IOException e) {
            System.err.println("❌ Error loading HomePage: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
