package uniearn.controller.contracts;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import uniearn.model.entities.contracts.Contrat;
import uniearn.services.contracts.ContratService;
import uniearn.utils.DialogUtil;

import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Contrôleur pour la création et la modification de contrats
 */
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
            populateFields(contrat);
        } else {
            initializeNewContrat();
        }
    }

    private void populateFields(Contrat contrat) {
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
    }

    private void initializeNewContrat() {
        this.contrat = new Contrat();
        dpStartDate.setValue(LocalDate.now());
        dpEndDate.setValue(LocalDate.now().plusMonths(1));
    }

    private void saveContrat() {
        try {
            if (!validateFields()) {
                return;
            }

            mapFieldsToContrat();
            persistContrat();

        } catch (NumberFormatException e) {
            DialogUtil.showError("Erreur de saisie",
                "Les montants et IDs doivent être des nombres valides");
        } catch (Exception e) {
            DialogUtil.showError("Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        if (tfAmount.getText().isEmpty() || dpStartDate.getValue() == null ||
            dpEndDate.getValue() == null || tfProjectID.getText().isEmpty() ||
            tfClientID.getText().isEmpty()) {
            DialogUtil.showWarning("Validation",
                "Veuillez remplir tous les champs obligatoires");
            return false;
        }
        return true;
    }

    private void mapFieldsToContrat() {
        contrat.setAmount(Double.parseDouble(tfAmount.getText()));
        contrat.setStartDate(Timestamp.valueOf(dpStartDate.getValue().atStartOfDay()));
        contrat.setEndDate(Timestamp.valueOf(dpEndDate.getValue().atStartOfDay()));
        contrat.setProjectID(Integer.parseInt(tfProjectID.getText()));
        contrat.setClientID(Integer.parseInt(tfClientID.getText()));

        if (!tfPaymentID.getText().isEmpty()) {
            contrat.setPaymentID(Integer.parseInt(tfPaymentID.getText()));
        }
    }

    private void persistContrat() {
        boolean success = (contrat.getIdContract() == 0) ?
            createNewContrat() :
            updateExistingContrat();

        if (success) {
            DialogUtil.showInfo("Succès", "Contrat enregistré avec succès");
            closeDialog();
        } else {
            DialogUtil.showError("Erreur", "Impossible d'enregistrer le contrat");
        }
    }

    private boolean createNewContrat() {
        contrat.setStatus(0);
        return contratService.createContrat(contrat);
    }

    private boolean updateExistingContrat() {
        return contratService.updateContrat(contrat);
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}

