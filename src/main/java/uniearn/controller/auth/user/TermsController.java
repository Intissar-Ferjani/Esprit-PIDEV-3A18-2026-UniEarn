package uniearn.controller.auth.user;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import uniearn.model.enums.UserRole;

import java.io.File;
import java.io.IOException;

public class TermsController {

    @FXML private Button acceptButton;
    @FXML private Button declineButton;

    private boolean openedFromSignup = false;
    private SignupController signupController;

    private String  snapshotName;
    private String  snapshotEmail;
    private String  snapshotPassword;
    private UserRole snapshotRole;
    private File    snapshotPhoto;


    public void setOpenedFromSignup(boolean fromSignup, SignupController controller) {
        this.openedFromSignup   = fromSignup;
        this.signupController   = controller;
    }


    public void setFormSnapshot(String name, String email, String password,
                                UserRole role, File profilePhoto) {
        this.snapshotName     = name;
        this.snapshotEmail    = email;
        this.snapshotPassword = password;
        this.snapshotRole     = role;
        this.snapshotPhoto    = profilePhoto;
    }

    // ── Button handlers ───────────────────────────────────────────────────

    @FXML
    private void handleAccept() {
        returnToSignup(true);
    }

    @FXML
    private void handleDecline() {
        returnToSignup(false);
    }

    @FXML
    private void handleBack() {
        returnToSignup(false);
    }

    // ── navigation ───────────────────────────────────────────────

    private void returnToSignup(boolean accepted) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/auth/signup/signup.fxml"));
            Parent root = loader.load();

            SignupController controller = loader.getController();

            // Restore the form + snapshotted data
            controller.restoreAfterTerms(
                    accepted,
                    snapshotName,
                    snapshotEmail,
                    snapshotPassword,
                    snapshotRole,
                    snapshotPhoto
            );

            Stage stage = (Stage) acceptButton.getScene().getWindow();
            stage.setScene(new Scene(root, 750, 600));
            stage.setTitle("Sign Up - UniEarn");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}