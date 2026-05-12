package uniearn.controller.profile.freelancer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import uniearn.model.entities.BankAccount;
import uniearn.services.BankAccountService;
import uniearn.model.enums.UserRole;

import java.util.List;

/**
 * Contrôleur pour la gestion des moyens de paiement (comptes bancaires) du freelancer
 */
public class FreelancerPaymentMethodsController {
    @FXML private TextField ibanField;
    @FXML private TextField swiftCodeField;
    @FXML private Button saveButton;
    @FXML private VBox successAlert;
    @FXML private Label messageLabel;

    private uniearn.services.users.freelancer.FreelancerService freelancerService;
    private int currentUserID;
    private uniearn.model.entities.users.freelancer.Freelancer currentFreelancer;

    @FXML
    public void initialize() {
        freelancerService = new uniearn.services.users.freelancer.FreelancerService();
        saveButton.setOnAction(e -> handleSaveCoordonnees());
    }

    /**
     * Charger les données du freelancer
     */
    public void setUserID(int userID) {
        this.currentUserID = userID;
        loadFreelancerData();
    }

    private void loadFreelancerData() {
        currentFreelancer = freelancerService.getFreelancerById(currentUserID);
        if (currentFreelancer != null) {
            ibanField.setText(currentFreelancer.getIban() != null ? currentFreelancer.getIban() : "");
            swiftCodeField.setText(currentFreelancer.getSwiftCode() != null ? currentFreelancer.getSwiftCode() : "");
        }
    }

    private void handleSaveCoordonnees() {
        String iban = ibanField.getText().trim();
        String swift = swiftCodeField.getText().trim();

        if (iban.isEmpty()) {
            showError("Veuillez saisir votre IBAN");
            return;
        }

        if (currentFreelancer == null) {
            showError("Erreur: Profil non chargé");
            return;
        }

        currentFreelancer.setIban(iban);
        currentFreelancer.setSwiftCode(swift);

        try {
            freelancerService.updateFreelancer(currentUserID, currentFreelancer);
            showSuccess();
        } catch (Exception e) {
            showError("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    private void showSuccess() {
        successAlert.setVisible(true);
        successAlert.setManaged(true);
        
        // Cacher après 5 secondes (optionnel)
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
        pause.setOnFinished(event -> {
            successAlert.setVisible(false);
            successAlert.setManaged(false);
        });
        pause.play();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

