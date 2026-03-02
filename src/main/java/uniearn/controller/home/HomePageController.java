package uniearn.controller.home;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.io.IOException;

public class HomePageController {

    @FXML private Button signupButton;

    @FXML
    public void initialize() {
        System.out.println("✓ HomeController initialized");
    }

    @FXML
    private void handleLogin() {
        navigateTo("/auth/login/login.fxml", "Connexion - UniEarn", 750, 600);
    }

    @FXML
    private void handleSignup() {
        navigateTo("/auth/signup/signup.fxml", "S'inscrire - UniEarn", 850, 700);
    }

    @FXML
    private void handleBrowse() {
        System.out.println("Browse missions clicked");
    }

    @FXML
    private void handleFeatures() {
        System.out.println("Features clicked");
    }

    @FXML
    private void handlePricing() {
        System.out.println("Pricing clicked");
    }

    @FXML
    private void handleAbout() {
        System.out.println("About clicked");
    }

    // ── Helper ──────────────────────────────────────
    private void navigateTo(String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = getCurrentStage();
            if (stage == null) stage = new Stage();

            stage.setScene(new Scene(root, width, height));
            stage.setTitle(title);
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Navigation error to: " + fxmlPath + " — " + e.getMessage());
        }
    }

    private Stage getCurrentStage() {
        try {
            if (signupButton != null && signupButton.getScene() != null) {
                return (Stage) signupButton.getScene().getWindow();
            }
        } catch (Exception ignored) {}
        return null;
    }
}