package uniearn.controller.contracts;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import uniearn.model.entities.contracts.Contrat;
import uniearn.services.contracts.ContratService;
import uniearn.controller.util.DialogUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Contrôleur pour la gestion des contrats
 * Affiche une table des contrats et permet les opérations CRUD
 */
public class ContratController {

    @FXML
    private TableView<Contrat> tableContracts;
    @FXML
    private TableColumn<Contrat, Integer> colId;
    @FXML
    private TableColumn<Contrat, String> colStartDate;
    @FXML
    private TableColumn<Contrat, String> colEndDate;
    @FXML
    private TableColumn<Contrat, Double> colAmount;
    @FXML
    private TableColumn<Contrat, String> colStatus;
    @FXML
    private TableColumn<Contrat, Integer> colClientID;
    @FXML
    private TableColumn<Contrat, Integer> colProjectID;
    @FXML
    private TableColumn<Contrat, Integer> colPaymentID;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnSignClient;
    @FXML
    private Button btnSignFreelancer;
    @FXML
    private Button btnRefresh;
    @FXML
    private Label lblStatus;
    @FXML
    private Label lblTotal;

    private ContratService contratService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final Logger logger = Logger.getLogger(ContratController.class.getName());

    @FXML
    public void initialize() {
        contratService = new ContratService();
        if (contratService == null) {
            System.err.println("Erreur: Le service des contrats ne peut pas être initialisé");
            return;
        }
        setupTableColumns();
        loadContrats();
        setupButtonActions();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getIdContract()).asObject());

        colStartDate.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStartDate() != null ?
                dateFormat.format(cellData.getValue().getStartDate()) : "N/A"));

        colEndDate.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getEndDate() != null ?
                dateFormat.format(cellData.getValue().getEndDate()) : "N/A"));

        colAmount.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());

        colStatus.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatusString()));

        colClientID.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getClientID()).asObject());

        colProjectID.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getProjectID()).asObject());

        colPaymentID.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getPaymentID()).asObject());
    }

    private void loadContrats() {
        try {
            List<Contrat> contrats = contratService.getAllContrats();
            ObservableList<Contrat> data = FXCollections.observableArrayList(contrats);
            tableContracts.setItems(data);
            lblTotal.setText("Total: " + contrats.size() + " contrat(s)");
            lblStatus.setText("✓ Données chargées avec succès");
        } catch (Exception e) {
            DialogUtil.showError("Erreur", "Impossible de charger les contrats: " + e.getMessage());
            lblStatus.setText("✗ Erreur de chargement");
        }
    }

    private void setupButtonActions() {
        btnAdd.setOnAction(e -> openContratDialog(null));
        btnEdit.setOnAction(e -> editContrat());
        btnDelete.setOnAction(e -> deleteContrat());
        btnSignClient.setOnAction(e -> signByClient());
        btnSignFreelancer.setOnAction(e -> signByFreelancer());
        btnRefresh.setOnAction(e -> loadContrats());
    }

    private void openContratDialog(Contrat contrat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contract_dialog.fxml"));
            Parent root = loader.load();

            ContratDialogController dialogController = loader.getController();
            dialogController.setContrat(contrat);

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 450, 350));
            stage.setTitle(contrat == null ? "Ajouter un Contrat" : "Modifier le Contrat");
            stage.setResizable(false);

            stage.setOnHidden(event -> loadContrats());
            stage.showAndWait();
        } catch (IOException ex) {
            DialogUtil.showError("Erreur", "Impossible d'ouvrir le dialogue: " + ex.getMessage());
        }
    }

    private void editContrat() {
        Contrat selected = tableContracts.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openContratDialog(selected);
        } else {
            DialogUtil.showWarning("Sélection requise", "Veuillez sélectionner un contrat à modifier");
        }
    }

    private void deleteContrat() {
        Contrat selected = tableContracts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Sélection requise", "Veuillez sélectionner un contrat à supprimer");
            return;
        }

        Optional<ButtonType> result = showConfirm("Confirmation",
            "Êtes-vous sûr de vouloir supprimer ce contrat ?");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (contratService.deleteContrat(selected.getIdContract())) {
                DialogUtil.showInfo("Succès", "Contrat supprimé avec succès");
                loadContrats();
            } else {
                DialogUtil.showError("Erreur", "Impossible de supprimer le contrat");
            }
        }
    }

    private void signByClient() {
        Contrat selected = tableContracts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Sélection requise", "Veuillez sélectionner un contrat");
            return;
        }

        if (contratService.signByClient(selected.getIdContract())) {
            DialogUtil.showInfo("Succès", "Contrat signé par le client");
            loadContrats();
        } else {
            DialogUtil.showError("Erreur", "Impossible de signer le contrat");
        }
    }

    private void signByFreelancer() {
        Contrat selected = tableContracts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Sélection requise", "Veuillez sélectionner un contrat");
            return;
        }

        if (contratService.signByFreelancer(selected.getIdContract())) {
            DialogUtil.showInfo("Succès", "Contrat signé par le freelancer");
            loadContrats();
        } else {
            DialogUtil.showError("Erreur", "Impossible de signer le contrat");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Optional<ButtonType> showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }
}

