package uniearn.controller.contracts.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uniearn.controller.contracts.contrat.ContractSignatureController;
import uniearn.controller.contracts.contrat.ContratDialogController;
import uniearn.model.entities.contracts.Contrat;
import uniearn.services.contracts.ContratService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion complète des contrats (Admin)
 * CRUD complet + validation + statistiques
 */
public class AdminContractController {

    @FXML private TableView<Contrat> contractsTable;
    @FXML private TableColumn<Contrat, Integer> colId;
    @FXML private TableColumn<Contrat, String> colType;
    @FXML private TableColumn<Contrat, Double> colAmount;
    @FXML private TableColumn<Contrat, Integer> colClientId;
    @FXML private TableColumn<Contrat, Integer> colProjectId;
    @FXML private TableColumn<Contrat, Integer> colFreelancerId;
    @FXML private TableColumn<Contrat, String> colStatus;
    @FXML private TableColumn<Contrat, String> colStartDate;
    @FXML private TableColumn<Contrat, Void> colActions;

    @FXML private Button btnNewContract;
    @FXML private Button btnManageTemplates;
    @FXML private Button btnRefresh;
    @FXML private ComboBox<String> cbFilterStatus;
    @FXML private TextField tfSearch;
    @FXML private Button btnSearch;
    @FXML private Label lblStats;
    @FXML private Label lblSummary;

    private ContratService contratService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private List<Contrat> allContracts;

    @FXML
    public void initialize() {
        contratService = new ContratService();
        setupTableColumns();

        // Initialiser le ComboBox de filtrage
        if (cbFilterStatus != null) {
            cbFilterStatus.setItems(FXCollections.observableArrayList(
                "Tous", "Brouillon", "En Attente de Signature", "Signé Client",
                "Signé Freelancer", "Actif", "Complété", "Annulé"
            ));
            cbFilterStatus.setValue("Tous");
        }

        loadContracts();
        setupButtonListeners();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idContract"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colClientId.setCellValueFactory(new PropertyValueFactory<>("clientID"));
        colProjectId.setCellValueFactory(new PropertyValueFactory<>("projectID"));
        colFreelancerId.setCellValueFactory(new PropertyValueFactory<>("freelancerID"));
        colStatus.setCellValueFactory(cellData -> {
            int status = cellData.getValue().getStatus();
            String statusText = getStatusText(status);
            return new javafx.beans.property.SimpleStringProperty(statusText);
        });
        colStartDate.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                dateFormat.format(cellData.getValue().getStartDate())
            )
        );

        // Colonne Actions
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️ Modifier");
            private final Button btnView = new Button("👁️ Voir");
            private final Button btnDelete = new Button("🗑️ Supprimer");

            {
                btnEdit.setStyle("-fx-padding: 5 10; -fx-font-size: 11;");
                btnView.setStyle("-fx-padding: 5 10; -fx-font-size: 11;");
                btnDelete.setStyle("-fx-padding: 5 10; -fx-font-size: 11; -fx-text-fill: white; -fx-background-color: #F44336;");

                btnEdit.setOnAction(event -> {
                    Contrat contract = getTableView().getItems().get(getIndex());
                    editContract(contract);
                });

                btnView.setOnAction(event -> {
                    Contrat contract = getTableView().getItems().get(getIndex());
                    viewContract(contract);
                });

                btnDelete.setOnAction(event -> {
                    Contrat contract = getTableView().getItems().get(getIndex());
                    deleteContract(contract);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox(5);
                    hBox.getChildren().addAll(btnEdit, btnView, btnDelete);
                    setGraphic(hBox);
                }
            }
        });
    }

    private void setupButtonListeners() {
        btnNewContract.setOnAction(e -> openNewContractDialog());
        btnManageTemplates.setOnAction(e -> openTemplatesManager());
        btnRefresh.setOnAction(e -> loadContracts());
        btnSearch.setOnAction(e -> filterContracts());
        cbFilterStatus.setOnAction(e -> filterContracts());
    }

    private void loadContracts() {
        try {
            allContracts = contratService.getAllContrats();
            updateTableView(allContracts);
            updateStatistics(allContracts);
        } catch (Exception e) {
            showError("Erreur lors du chargement des contrats", e.getMessage());
        }
    }

    private void updateTableView(List<Contrat> contracts) {
        ObservableList<Contrat> observableList = FXCollections.observableArrayList(contracts);
        contractsTable.setItems(observableList);
    }

    private void filterContracts() {
        String statusFilter = cbFilterStatus.getValue();
        String searchText = tfSearch.getText().toLowerCase();

        List<Contrat> filtered = allContracts.stream()
            .filter(c -> statusFilter == null || statusFilter.equals("Tous") || getStatusText(c.getStatus()).equals(statusFilter))
            .filter(c -> searchText.isEmpty() ||
                    String.valueOf(c.getIdContract()).contains(searchText) ||
                    c.getType().toLowerCase().contains(searchText))
            .collect(Collectors.toList());

        updateTableView(filtered);
        updateStatistics(filtered);
    }

    private void updateStatistics(List<Contrat> contracts) {
        int total = contracts.size();
        long actifs = contracts.stream().filter(c -> c.getStatus() == 3).count();
        long signes = contracts.stream().filter(c -> c.getStatus() >= 1).count();
        long completes = contracts.stream().filter(c -> c.getStatus() == 3).count();
        long annules = contracts.stream().filter(c -> c.getStatus() == 4).count();

        lblStats.setText(String.format("Total: %d contrats", total));
        lblSummary.setText(String.format("Actifs: %d | Signés: %d | Complétés: %d | Annulés: %d",
            actifs, signes, completes, annules));
    }

    private void openNewContractDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contract_dialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Créer un Contrat");
            stage.setScene(new Scene(root, 600, 500));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadContracts();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la fenêtre de création");
        }
    }

    private void editContract(Contrat contract) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contract_dialog.fxml"));
            Parent root = loader.load();

            ContratDialogController controller = loader.getController();
            controller.setContrat(contract);

            Stage stage = new Stage();
            stage.setTitle("Modifier le Contrat #" + contract.getIdContract());
            stage.setScene(new Scene(root, 600, 500));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadContracts();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la fenêtre de modification");
        }
    }

    private void viewContract(Contrat contract) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contract_signature.fxml"));
            Parent root = loader.load();

            ContractSignatureController controller = loader.getController();
            controller.setContract(contract);

            Stage stage = new Stage();
            stage.setTitle("Détails du Contrat #" + contract.getIdContract());
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir les détails du contrat");
        }
    }

    private void deleteContract(Contrat contract) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le contrat?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le contrat #" + contract.getIdContract() + "?");

        if (alert.showAndWait().isPresent() && alert.showAndWait().get() == ButtonType.OK) {
            try {
                contratService.deleteContrat(contract.getIdContract());
                showSuccess("Succès", "Le contrat a été supprimé");
                loadContracts();
            } catch (Exception e) {
                showError("Erreur", "Impossible de supprimer le contrat: " + e.getMessage());
            }
        }
    }

    private void openTemplatesManager() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contract_template_admin.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion des Templates");
            stage.setScene(new Scene(root, 900, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le gestionnaire de templates");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getStatusText(int status) {
        return switch (status) {
            case 0 -> "Brouillon";
            case 1 -> "Signé Client";
            case 2 -> "Signé Freelancer";
            case 3 -> "Actif";
            case 4 -> "Annulé";
            default -> "Inconnu";
        };
    }
}

