package uniearn.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import uniearn.model.entities.Contrat;
import uniearn.services.ContratService;

import java.sql.Timestamp;
import java.time.LocalDate;

public class ContratDialogController {

    @FXML
    private TextField tfAmount;
    @FXML
    private DatePicker dpStartDate;
    @FXML
    private DatePicker dpEndDate;
    @FXML
    private TextField tfProjectID;
    @FXML
    private TextField tfClientID;
    @FXML
    private TextField tfPaymentID;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    private Contrat contrat;
    private ContratService contratService;

    @FXML
    public void initialize() {
        contratService = new ContratService();
        btnSave.setOnAction(e -> saveContrat());
        btnCancel.setOnAction(e -> closeDialog());
    }

    public void setContrat(Contrat contrat) {
        this.contrat = contrat;
        if (contrat != null) {
            // Mode édition
            tfAmount.setText(String.valueOf(contrat.getAmount()));
            if (contrat.getStartDate() != null) {
                dpStartDate.setValue(contrat.getStartDate().toLocalDateTime().toLocalDate());
            }
            if (contrat.getEndDate() != null) {
                dpEndDate.setValue(contrat.getEndDate().toLocalDateTime().toLocalDate());
            }
            tfProjectID.setText(String.valueOf(contrat.getProjectID()));
            tfClientID.setText(String.valueOf(contrat.getClientID()));
            tfPaymentID.setText(String.valueOf(contrat.getPaymentID()));
        } else {
            // Mode création
            contrat = new Contrat();
            dpStartDate.setValue(LocalDate.now());
            dpEndDate.setValue(LocalDate.now().plusMonths(1));
        }
    }

    private void saveContrat() {
        try {
            // Validation des champs
            if (tfAmount.getText().isEmpty() || dpStartDate.getValue() == null ||
                dpEndDate.getValue() == null || tfProjectID.getText().isEmpty() ||
                tfClientID.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs obligatoires");
                return;
            }

            // Remplir l'objet Contrat
            contrat.setAmount(Double.parseDouble(tfAmount.getText()));
            contrat.setStartDate(Timestamp.valueOf(dpStartDate.getValue().atStartOfDay()));
            contrat.setEndDate(Timestamp.valueOf(dpEndDate.getValue().atStartOfDay()));
            contrat.setProjectID(Integer.parseInt(tfProjectID.getText()));
            contrat.setClientID(Integer.parseInt(tfClientID.getText()));

            // PaymentID est optionnel
            if (!tfPaymentID.getText().isEmpty()) {
                contrat.setPaymentID(Integer.parseInt(tfPaymentID.getText()));
            }

            boolean success;
            if (contrat.getIdContract() == 0) {
                // Création
                contrat.setStatus(0); // Brouillon par défaut
                success = contratService.createContrat(contrat);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Contrat créé avec succès");
                }
            } else {
                // Mise à jour
                success = contratService.updateContrat(contrat);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Contrat mis à jour avec succès");
                }
            }

            if (success) {
                closeDialog();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'enregistrer le contrat");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                "Les montants et IDs doivent être des nombres valides");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

