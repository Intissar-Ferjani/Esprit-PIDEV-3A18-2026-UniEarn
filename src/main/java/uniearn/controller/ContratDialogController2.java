package uniearn.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import uniearn.crud.ContratCRUD;
import uniearn.model.entities.Contrat;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la boîte de dialogue d'ajout de contrat
 * Interface moderne avec validation en temps réel
 */
public class ContratDialogController2 implements Initializable {

    // ===== FXML Components =====
    @FXML private TextField clientIDField;
    @FXML private TextField projectIDField;
    @FXML private TextField amountField;
    @FXML private TextField paymentIDField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Label errorLabel;
    @FXML private Label statusLabel;
    @FXML private Button addButton;
    @FXML private Button resetButton;
    @FXML private Button cancelButton;

    // ===== Variables =====
    private ContratCRUD crud;
    private Stage stage;
    private Contrat contratAjoute;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.crud = new ContratCRUD();

        // Initialiser les statuts
        statusComboBox.getItems().addAll(
            "0 - Brouillon",
            "1 - Signé Client",
            "2 - Signé Freelancer",
            "3 - Complété"
        );
        statusComboBox.setValue("0 - Brouillon");

        // Initialiser les dates
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusMonths(1));

        // Ajouter les event handlers
        addButton.setOnAction(e -> ajouterContrat());
        resetButton.setOnAction(e -> reinitialiserFormulaire());
        cancelButton.setOnAction(e -> fermerDialog());

        // Ajouter les validations en temps réel
        clientIDField.textProperty().addListener((obs, old, newVal) -> validerChamps());
        projectIDField.textProperty().addListener((obs, old, newVal) -> validerChamps());
        amountField.textProperty().addListener((obs, old, newVal) -> validerChamps());
        startDatePicker.valueProperty().addListener((obs, old, newVal) -> validerChamps());
        endDatePicker.valueProperty().addListener((obs, old, newVal) -> validerChamps());

        afficherMessage("Prêt à ajouter un contrat", Color.web("#4CAF50"));
    }

    /**
     * Ajouter un contrat à la base de données
     */
    @FXML
    private void ajouterContrat() {
        try {
            // Valider les champs obligatoires
            if (!validerChamps()) {
                afficherMessage("Veuillez remplir tous les champs obligatoires", Color.web("#FF6B6B"));
                return;
            }

            // Récupérer les valeurs
            int clientID = Integer.parseInt(clientIDField.getText().trim());
            int projectID = Integer.parseInt(projectIDField.getText().trim());
            double amount = Double.parseDouble(amountField.getText().trim());

            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            // Valider les dates
            if (startDate == null || endDate == null) {
                afficherMessage("Veuillez sélectionner les dates", Color.web("#FF6B6B"));
                return;
            }

            if (endDate.isBefore(startDate)) {
                afficherMessage("La date de fin doit être après la date de début", Color.web("#FF6B6B"));
                return;
            }

            if (amount <= 0) {
                afficherMessage("Le montant doit être positif", Color.web("#FF6B6B"));
                return;
            }

            // Récupérer le statut
            String statusStr = statusComboBox.getValue();
            int status = Integer.parseInt(statusStr.split(" - ")[0]);

            // Récupérer le paiement (optionnel)
            int paymentID = 0;
            if (!paymentIDField.getText().trim().isEmpty()) {
                try {
                    paymentID = Integer.parseInt(paymentIDField.getText().trim());
                } catch (NumberFormatException e) {
                    afficherMessage("ID Paiement invalide", Color.web("#FF6B6B"));
                    return;
                }
            }

            // Créer le contrat
            Contrat contrat = new Contrat();
            contrat.setClientID(clientID);
            contrat.setProjectID(projectID);
            contrat.setAmount(amount);
            contrat.setStatus(status);
            contrat.setPaymentID(paymentID);
            contrat.setStartDate(Timestamp.valueOf(startDate.atStartOfDay()));
            contrat.setEndDate(Timestamp.valueOf(endDate.atTime(23, 59, 59)));

            // Ajouter à la BD
            if (crud.create(contrat)) {
                this.contratAjoute = contrat;
                afficherMessage("✓ Contrat ajouté avec succès !", Color.web("#4CAF50"));

                // Fermer après 1.5 secondes
                Thread.sleep(1500);
                fermerDialog();
            } else {
                afficherMessage("✗ Erreur lors de l'ajout du contrat", Color.web("#FF6B6B"));
            }

        } catch (NumberFormatException e) {
            afficherMessage("Veuillez entrer des nombres valides", Color.web("#FF6B6B"));
        } catch (Exception e) {
            afficherMessage("Erreur : " + e.getMessage(), Color.web("#FF6B6B"));
            e.printStackTrace();
        }
    }

    /**
     * Valider les champs obligatoires
     */
    private boolean validerChamps() {
        boolean valide = true;
        errorLabel.setText("");

        // Valider Client ID
        if (clientIDField.getText().trim().isEmpty()) {
            valide = false;
        } else {
            try {
                Integer.parseInt(clientIDField.getText().trim());
            } catch (NumberFormatException e) {
                valide = false;
            }
        }

        // Valider Project ID
        if (projectIDField.getText().trim().isEmpty()) {
            valide = false;
        } else {
            try {
                Integer.parseInt(projectIDField.getText().trim());
            } catch (NumberFormatException e) {
                valide = false;
            }
        }

        // Valider Amount
        if (amountField.getText().trim().isEmpty()) {
            valide = false;
        } else {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) {
                    valide = false;
                }
            } catch (NumberFormatException e) {
                valide = false;
            }
        }

        // Valider dates
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            valide = false;
        } else if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            valide = false;
        }

        // Mettre à jour le style du bouton
        if (valide) {
            addButton.setStyle("-fx-padding: 12 30; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12; -fx-font-weight: bold; -fx-border-radius: 5; -fx-cursor: hand;");
        } else {
            addButton.setStyle("-fx-padding: 12 30; -fx-background-color: #CCCCCC; -fx-text-fill: white; -fx-font-size: 12; -fx-font-weight: bold; -fx-border-radius: 5;");
        }

        return valide;
    }

    /**
     * Réinitialiser le formulaire
     */
    @FXML
    private void reinitialiserFormulaire() {
        clientIDField.clear();
        projectIDField.clear();
        amountField.clear();
        paymentIDField.clear();
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusMonths(1));
        statusComboBox.setValue("0 - Brouillon");
        errorLabel.setText("");
        afficherMessage("Formulaire réinitialisé", Color.web("#1E56DB"));
    }

    /**
     * Fermer la boîte de dialogue
     */
    @FXML
    private void fermerDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Afficher un message à l'utilisateur
     */
    private void afficherMessage(String message, Color couleur) {
        statusLabel.setText(message);
        statusLabel.setTextFill(couleur);
    }

    /**
     * Obtenir le contrat ajouté
     */
    public Contrat getContratAjoute() {
        return contratAjoute;
    }

    /**
     * Définir le stage (optionnel)
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}

