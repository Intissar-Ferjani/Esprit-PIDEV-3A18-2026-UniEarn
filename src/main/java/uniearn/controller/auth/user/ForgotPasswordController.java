package uniearn.controller.auth.user;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import uniearn.services.users.UserService;
import uniearn.services.users.mail.EmailService;

import java.io.IOException;

public class ForgotPasswordController {

    // ── Step 1: Enter email ──
    @FXML
    private TextField emailField;
    @FXML
    private Label emailError;
    @FXML
    private Button sendCodeButton;

    // ── Step 2: Enter code + new password ──
    @FXML
    private TextField codeField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label codeError;
    @FXML
    private Label newPasswordError;
    @FXML
    private Label confirmPasswordError;
    @FXML
    private Button resetButton;

    // ── Layout sections ──
    @FXML
    private javafx.scene.layout.VBox emailStep;
    @FXML
    private javafx.scene.layout.VBox resetStep;
    @FXML
    private Label statusLabel;

    private final UserService userService = new UserService();
    private final EmailService emailService = new EmailService();
    private String pendingEmail;

    @FXML
    public void initialize() {
        // Start on step 1
        emailStep.setVisible(true);
        emailStep.setManaged(true);
        resetStep.setVisible(false);
        resetStep.setManaged(false);
    }

    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError(emailError, "Veuillez entrer votre adresse email");
            return;
        }

        sendCodeButton.setDisable(true);
        sendCodeButton.setText("Envoi en cours...");
        statusLabel.setText("");

        // Send on background thread so UI doesn't freeze
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                String token = userService.generateResetToken(email);
                if (token == null)
                    return false; // email not found

                emailService.sendPasswordResetEmail(email, token);
                return true;
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                pendingEmail = email;
                // Switch to step 2
                emailStep.setVisible(false);
                emailStep.setManaged(false);
                resetStep.setVisible(true);
                resetStep.setManaged(true);
                statusLabel.setText("✅ Code envoyé à " + email);
                statusLabel.setStyle("-fx-text-fill: #16a34a;");
            } else {
                showError(emailError, "Aucun compte trouvé avec cet email");
                sendCodeButton.setDisable(false);
                sendCodeButton.setText("Envoyer le code");
            }
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                showError(emailError, "Erreur d'envoi: " + task.getException().getMessage());
                sendCodeButton.setDisable(false);
                sendCodeButton.setText("Envoyer le code");
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleResetPassword() {
        String code = codeField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        boolean valid = true;

        if (code.isEmpty()) {
            showError(codeError, "Veuillez entrer le code reçu");
            valid = false;
        } else if (!userService.validateResetToken(pendingEmail, code)) {
            showError(codeError, "❌ Code incorrect ou expiré");
            valid = false;
        } else {
            hideError(codeError);
        }

        if (newPassword.length() < 8) {
            showError(newPasswordError, "Minimum 8 caractères");
            valid = false;
        } else if (!newPassword.matches(".*[A-Z].*") || !newPassword.matches(".*[a-z].*")
                || !newPassword.matches(".*\\d.*")) {
            showError(newPasswordError, "Doit contenir majuscule, minuscule et chiffre");
            valid = false;
        } else {
            hideError(newPasswordError);
        }

        if (!newPassword.equals(confirmPassword)) {
            showError(confirmPasswordError, "Les mots de passe ne correspondent pas");
            valid = false;
        } else {
            hideError(confirmPasswordError);
        }

        if (!valid)
            return;

        try {
            var user = userService.getUserByEmail(pendingEmail);
            userService.updatePassword(user.getIdUser(), newPassword);
            userService.clearResetToken(pendingEmail);

            showSuccessAndRedirect();
        } catch (Exception e) {
            showError(codeError, "Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showSuccessAndRedirect() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mot de passe réinitialisé");
        alert.setHeaderText(null);
        alert.setContentText(
                "✅ Votre mot de passe a été réinitialisé avec succès!\n\nVous pouvez maintenant vous connecter.");
        alert.showAndWait();
        navigateToLogin();
    }

    @FXML
    private void handleBackToLogin() {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/login/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 750, 600));
            stage.setTitle("Connexion - UniEarn");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(Label label, String msg) {
        label.setText(msg);
        label.setVisible(true);
    }

    private void hideError(Label label) {
        label.setText("");
        label.setVisible(false);
    }
}