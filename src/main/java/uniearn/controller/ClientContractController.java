package uniearn.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uniearn.model.entities.Contrat;
import uniearn.services.ContratService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Contrôleur pour la gestion des contrats côté Client
 */
public class ClientContractController {

    @FXML private TableView<Contrat> contractsTable;
    @FXML private TableColumn<Contrat, Integer> colId;
    @FXML private TableColumn<Contrat, String> colType;
    @FXML private TableColumn<Contrat, Double> colAmount;
    @FXML private TableColumn<Contrat, Integer> colProjectId;
    @FXML private TableColumn<Contrat, Integer> colFreelancer;
    @FXML private TableColumn<Contrat, String> colStatus;
    @FXML private TableColumn<Contrat, Void> colActions;

    @FXML private Button btnNewContract;
    @FXML private Button btnRefresh;
    @FXML private Label lblStats;
    @FXML private Label lblSummary;

    private ContratService contratService;
    private int currentClientID = 1; // À récupérer de la session utilisateur
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @FXML
    public void initialize() {
        contratService = new ContratService();
        setupTableColumns();
        loadContracts();
        setupButtonListeners();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idContract"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colProjectId.setCellValueFactory(new PropertyValueFactory<>("projectID"));
        colFreelancer.setCellValueFactory(new PropertyValueFactory<>("freelancerID"));
        colStatus.setCellValueFactory(cellData -> {
            Contrat contrat = cellData.getValue();
            String status = contrat.getStatusString();
            return new javafx.beans.property.SimpleStringProperty(status);
        });

        // Colonne Actions
        colActions.setCellFactory(param -> new TableCell<Contrat, Void>() {
            private final Button btnView = new Button("👁️ Voir");
            private final Button btnSign = new Button("✍️ Signer");
            private final Button btnDelete = new Button("🗑️");

            {
                btnView.setStyle("-fx-padding: 5 10; -fx-font-size: 10;");
                btnSign.setStyle("-fx-padding: 5 10; -fx-font-size: 10; -fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-padding: 5 10; -fx-font-size: 10; -fx-text-fill: white; -fx-background-color: #F44336;");

                btnView.setOnAction(event -> {
                    Contrat contrat = getTableView().getItems().get(getIndex());
                    viewContract(contrat);
                });

                btnSign.setOnAction(event -> {
                    Contrat contrat = getTableView().getItems().get(getIndex());
                    signContract(contrat);
                });

                btnDelete.setOnAction(event -> {
                    Contrat contrat = getTableView().getItems().get(getIndex());
                    deleteContract(contrat);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox(5);
                    hBox.getChildren().addAll(btnView, btnSign, btnDelete);
                    setGraphic(hBox);
                }
            }
        });
    }

    private void setupButtonListeners() {
        btnNewContract.setOnAction(e -> openNewContractDialog());
        btnRefresh.setOnAction(e -> loadContracts());
    }

    private void loadContracts() {
        // Pour le client connecté
        List<Contrat> contrats = contratService.getContratsByClient(currentClientID);
        ObservableList<Contrat> data = FXCollections.observableArrayList(contrats);
        contractsTable.setItems(data);
        updateStats();
    }

    private void updateStats() {
        int total = contractsTable.getItems().size();

        long drafted = contractsTable.getItems().stream()
            .filter(c -> c.getStatus() == 0).count();
        long signed = contractsTable.getItems().stream()
            .filter(c -> c.getStatus() >= 1 && c.getStatus() < 3).count();
        long completed = contractsTable.getItems().stream()
            .filter(c -> c.getStatus() == 3).count();

        lblStats.setText("Total: " + total + " contrats");
        lblSummary.setText("En cours: " + drafted + " | Signés: " + signed + " | Complétés: " + completed);
    }

    private void openNewContractDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/client_contract_dialog.fxml"));
            Parent root = loader.load();

            ClientContractDialogController dialogController = loader.getController();
            dialogController.setClientID(currentClientID);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Créer un Contrat");
            dialogStage.setScene(new Scene(root, 800, 700));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnNewContract.getScene().getWindow());

            dialogController.setDialogStage(dialogStage);
            dialogController.setOnContractCreated(contrat -> {
                loadContracts();
                showAlert("Succès", "Contrat créé avec succès", Alert.AlertType.INFORMATION);
                dialogStage.close();
            });

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ouverture du dialog", Alert.AlertType.ERROR);
        }
    }

    private void viewContract(Contrat contrat) {
        openSignatureDialog(contrat);
    }

    private void signContract(Contrat contrat) {
        openSignatureDialog(contrat);
    }

    private void openSignatureDialog(Contrat contrat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contract_signature.fxml"));
            Parent root = loader.load();

            ContractSignatureController signController = loader.getController();
            signController.setContract(contrat);
            signController.setContratService(contratService);

            Stage signStage = new Stage();
            signStage.setTitle("Signature du Contrat");
            signStage.setScene(new Scene(root, 900, 800));
            signStage.initModality(Modality.WINDOW_MODAL);
            signStage.initOwner(btnNewContract.getScene().getWindow());

            signController.setDialogStage(signStage);
            signController.setOnSignatureComplete(() -> {
                loadContracts();
                signStage.close();
            });

            signStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ouverture du dialog", Alert.AlertType.ERROR);
        }
    }

    private void deleteContract(Contrat contrat) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer le contrat?");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer le contrat #" + contrat.getIdContract() + "?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            if (contratService.deleteContrat(contrat.getIdContract())) {
                loadContracts();
                showAlert("Succès", "Contrat supprimé avec succès", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Erreur lors de la suppression", Alert.AlertType.ERROR);
            }
        }
    }

    public void setCurrentClientID(int clientID) {
        this.currentClientID = clientID;
        loadContracts();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

