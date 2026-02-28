package uniearn.controller.contracts.freelancer;

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
import uniearn.model.entities.contracts.Contrat;
import uniearn.services.contracts.ContratService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des contrats côté Freelancer
 * Consultation et signature des contrats
 */
public class FreelancerContractController {

    @FXML private TableView<Contrat> contractsTable;
    @FXML private TableColumn<Contrat, Integer> colId;
    @FXML private TableColumn<Contrat, String> colType;
    @FXML private TableColumn<Contrat, Double> colAmount;
    @FXML private TableColumn<Contrat, Integer> colClientId;
    @FXML private TableColumn<Contrat, Integer> colProjectId;
    @FXML private TableColumn<Contrat, String> colStatus;
    @FXML private TableColumn<Contrat, String> colStartDate;
    @FXML private TableColumn<Contrat, String> colEndDate;
    @FXML private TableColumn<Contrat, Void> colActions;

    @FXML private Button btnViewDetails;
    @FXML private Button btnSignContract;
    @FXML private Button btnRefresh;
    @FXML private ComboBox<String> cbFilterStatus;
    @FXML private TextField tfSearch;
    @FXML private Button btnSearch;
    @FXML private Label lblStats;
    @FXML private Label lblSummary;

    private ContratService contratService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private List<Contrat> allContracts;
    private int currentFreelancerID = 1; // À récupérer de la session utilisateur

    @FXML
    public void initialize() {
        contratService = new ContratService();
        setupTableColumns();

        // Initialiser le ComboBox de filtrage
        if (cbFilterStatus != null) {
            cbFilterStatus.setItems(FXCollections.observableArrayList(
                "Tous", "En Attente de Signature", "Signé Client", "Actif", "Complété"
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
        colEndDate.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                dateFormat.format(cellData.getValue().getEndDate())
            )
        );

        // Colonne Actions
        colActions.setCellFactory(param -> new TableCell<Contrat, Void>() {
            private final Button btnView = new Button("👁️ Voir");
            private final Button btnSign = new Button("✍️ Signer");

            {
                btnView.setStyle("-fx-padding: 5 10; -fx-font-size: 11;");
                btnSign.setStyle("-fx-padding: 5 10; -fx-font-size: 11; -fx-background-color: #FF9800; -fx-text-fill: white;");

                btnView.setOnAction(event -> {
                    Contrat contract = getTableView().getItems().get(getIndex());
                    viewContract(contract);
                });

                btnSign.setOnAction(event -> {
                    Contrat contract = getTableView().getItems().get(getIndex());
                    signContract(contract);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox(5);
                    hBox.getChildren().addAll(btnView, btnSign);
                    setGraphic(hBox);
                }
            }
        });
    }

    private void setupButtonListeners() {
        btnViewDetails.setOnAction(e -> {
            Contrat selected = contractsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                viewContract(selected);
            } else {
                showWarning("Attention", "Veuillez sélectionner un contrat");
            }
        });

        btnSignContract.setOnAction(e -> {
            Contrat selected = contractsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                signContract(selected);
            } else {
                showWarning("Attention", "Veuillez sélectionner un contrat");
            }
        });

        btnRefresh.setOnAction(e -> loadContracts());
        btnSearch.setOnAction(e -> filterContracts());
        cbFilterStatus.setOnAction(e -> filterContracts());
    }

    private void loadContracts() {
        try {
            // Récupérer les contrats assignés au freelancer actuel
            allContracts = contratService.getContratsByFreelancer(currentFreelancerID);
            updateTableView(allContracts);
            updateStatistics(allContracts);
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les contrats: " + e.getMessage());
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
        long toSign = contracts.stream().filter(c -> c.getStatus() == 1).count();
        long actifs = contracts.stream().filter(c -> c.getStatus() == 3).count();
        long completes = contracts.stream().filter(c -> c.getStatus() == 3).count();

        lblStats.setText(String.format("Total: %d contrats", total));
        lblSummary.setText(String.format("À signer: %d | Actifs: %d | Complétés: %d",
            toSign, actifs, completes));
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

            loadContracts();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir les détails du contrat");
        }
    }

    private void signContract(Contrat contract) {
        // Vérifier si le contrat peut être signé par le freelancer
        if (contract.getStatus() == 1) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contract_signature.fxml"));
                Parent root = loader.load();

                ContractSignatureController controller = loader.getController();
                controller.setContract(contract);

                Stage stage = new Stage();
                stage.setTitle("Signer le Contrat #" + contract.getIdContract());
                stage.setScene(new Scene(root, 800, 600));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

                loadContracts();
            } catch (IOException e) {
                showError("Erreur", "Impossible d'ouvrir la fenêtre de signature");
            }
        } else {
            showWarning("Attention", "Ce contrat ne peut pas être signé maintenant.\nStatut actuel: " + getStatusText(contract.getStatus()));
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getStatusText(int status) {
        switch (status) {
            case 0:
                return "Brouillon";
            case 1:
                return "Signé Client";
            case 2:
                return "Signé Freelancer";
            case 3:
                return "Actif";
            case 4:
                return "Annulé";
            default:
                return "Inconnu";
        }
    }

    public void setCurrentFreelancerID(int freelancerID) {
        this.currentFreelancerID = freelancerID;
        loadContracts();
    }
}

