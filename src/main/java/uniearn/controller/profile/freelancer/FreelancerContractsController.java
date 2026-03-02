package uniearn.controller.profile.freelancer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.model.entities.contracts.Contrat;
import uniearn.services.contracts.ContratService;

import java.io.IOException;
import java.util.List;

/**
 * Contrôleur pour afficher les contrats du freelancer dans son profil
 */
public class FreelancerContractsController {

    @FXML private VBox contractsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label noContractsLabel;
    @FXML private ScrollPane scrollPane;

    private TableView<Contrat> contractsTable;
    private Freelancer currentFreelancer;
    private final ContratService contratService = new ContratService();
    private final ObservableList<Contrat> contractsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        createContractsTable();
        setupFilters();
        setupSearch();
    }

    private void createContractsTable() {
        contractsTable = new TableView<>();
        contractsTable.setPrefHeight(400);
        contractsTable.setStyle("-fx-font-size: 12;");

        // Colonne ID
        TableColumn<Contrat, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idContract"));
        colId.setPrefWidth(50);

        // Colonne Type
        TableColumn<Contrat, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setPrefWidth(120);

        // Colonne Montant
        TableColumn<Contrat, Double> colAmount = new TableColumn<>("Montant (TND)");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setPrefWidth(100);

        // Colonne Statut
        TableColumn<Contrat, String> colStatus = new TableColumn<>("Statut");
        colStatus.setCellValueFactory(cellData -> {
            int status = cellData.getValue().getStatus();
            String statusStr = getStatusString(status);
            return new javafx.beans.property.SimpleStringProperty(statusStr);
        });
        colStatus.setPrefWidth(120);

        // Colonne Date Début
        TableColumn<Contrat, String> colStartDate = new TableColumn<>("Date Début");
        colStartDate.setCellValueFactory(cellData -> {
            String date = cellData.getValue().getStartDate() != null ? cellData.getValue().getStartDate().toString() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(date);
        });
        colStartDate.setPrefWidth(100);

        // Colonne Actions
        TableColumn<Contrat, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(200);
        colActions.setCellFactory(param -> new TableCell<Contrat, Void>() {
            private final HBox pane = new HBox(5);

            {
                Button signBtn = new Button("✍ Signer");
                signBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11;");
                signBtn.setOnAction(event -> openSignatureDialog(getTableView().getItems().get(getIndex())));

                Button viewBtn = new Button("👁 Détails");
                viewBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11;");
                viewBtn.setOnAction(event -> showContractDetails(getTableView().getItems().get(getIndex())));

                Button downloadBtn = new Button("📥 PDF");
                downloadBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11;");
                downloadBtn.setOnAction(event -> downloadContractPDF(getTableView().getItems().get(getIndex())));

                pane.getChildren().addAll(signBtn, viewBtn, downloadBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });

        contractsTable.getColumns().addAll(colId, colType, colAmount, colStatus, colStartDate, colActions);
        contractsTable.setItems(contractsList);

        scrollPane.setContent(contractsTable);
    }

    private String getStatusString(int status) {
        return switch (status) {
            case 0 -> "Brouillon";
            case 1 -> "En attente (Client)";
            case 2 -> "En attente (Freelancer)";
            case 3 -> "Actif";
            case 4 -> "Annulé";
            default -> "Inconnu";
        };
    }

    public void setFreelancerData(Freelancer freelancer) {
        this.currentFreelancer = freelancer;
        loadContracts();
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList(
                "Tous les statuts",
                "Brouillon",
                "En attente (Client)",
                "En attente (Freelancer)",
                "Actif",
                "Annulé"
        ));
        statusFilter.setValue("Tous les statuts");

        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentFreelancer != null) {
                loadContracts();
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (currentFreelancer != null) {
                loadContracts();
            }
        });
    }

    private void loadContracts() {
        if (currentFreelancer == null) return;

        try {
            System.out.println("DEBUG: loadContracts() - Freelancer ID = " + currentFreelancer.getIdUser());
            List<Contrat> contracts = contratService.getAllContrats();
            System.out.println("DEBUG: Total contracts in DB: " + contracts.size());

            contracts = contracts.stream()
                    .peek(c -> System.out.println("DEBUG: Checking contract ID=" + c.getIdContract() +
                           " with freelancerID=" + c.getFreelancerID()))
                    .filter(c -> c.getFreelancerID() == currentFreelancer.getIdUser())
                    .toList();

            System.out.println("DEBUG: Filtered contracts: " + contracts.size());

            String searchText = searchField.getText().toLowerCase();
            String statusFilterValue = this.statusFilter.getValue();

            List<Contrat> filtered = contracts.stream()
                    .filter(c -> filterByStatus(c, statusFilterValue))
                    .filter(c -> filterBySearch(c, searchText))
                    .toList();

            contractsList.setAll(filtered);
            displayContracts(filtered);

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des contrats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean filterByStatus(Contrat contract, String status) {
        if (status.equals("Tous les statuts")) return true;

        int contractStatus = contract.getStatus();
        String statusStr = getStatusString(contractStatus);
        return statusStr.equalsIgnoreCase(status);
    }

    private boolean filterBySearch(Contrat contract, String searchText) {
        if (searchText.isEmpty()) return true;
        String type = contract.getType() != null ? contract.getType().toLowerCase() : "";
        String id = String.valueOf(contract.getIdContract());
        return type.contains(searchText) || id.contains(searchText);
    }

    private void displayContracts(List<Contrat> contracts) {
        contractsList.setAll(contracts);

        if (contracts.isEmpty()) {
            noContractsLabel.setText("Aucun contrat trouvé");
            noContractsLabel.setVisible(true);
        } else {
            noContractsLabel.setVisible(false);
        }
    }

    private void showContractDetails(Contrat contract) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Détails du Contrat");
        dialog.initModality(Modality.APPLICATION_MODAL);

        DialogPane dialogPane = new DialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        VBox header = new VBox(5);
        header.setStyle("-fx-background-color: #1976d2; -fx-padding: 20px;");
        Label headerLabel = new Label("📄 Détails du Contrat");
        headerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        header.getChildren().add(headerLabel);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");

        grid.add(createLabel("ID Contrat:"), 0, 0);
        grid.add(new Label(String.valueOf(contract.getIdContract())), 1, 0);

        grid.add(createLabel("Type:"), 0, 1);
        grid.add(new Label(contract.getType() != null ? contract.getType() : "N/A"), 1, 1);

        grid.add(createLabel("Montant:"), 0, 2);
        grid.add(new Label(String.format("%.2f TND", contract.getAmount())), 1, 2);

        grid.add(createLabel("Statut:"), 0, 3);
        grid.add(new Label(getStatusString(contract.getStatus())), 1, 3);

        grid.add(createLabel("Date de Création:"), 0, 4);
        grid.add(new Label(contract.getStartDate() != null ? contract.getStartDate().toString() : "N/A"), 1, 4);

        grid.add(createLabel("Date de Fin:"), 0, 5);
        grid.add(new Label(contract.getEndDate() != null ? contract.getEndDate().toString() : "N/A"), 1, 5);

        VBox content = new VBox();
        content.getChildren().addAll(header, grid);

        dialogPane.setContent(content);
        dialogPane.getButtonTypes().add(ButtonType.OK);

        dialog.setDialogPane(dialogPane);
        dialog.showAndWait();
    }

    private void openSignatureDialog(Contrat contract) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contract_signature.fxml"));
            Parent root = loader.load();

            uniearn.controller.contracts.contrat.ContractSignatureController signatureController = loader.getController();
            signatureController.setContract(contract);
            signatureController.setContratService(contratService);
            signatureController.setUserType("FREELANCER"); // Indiquer que c'est un freelancer
            signatureController.setOnSignatureComplete(() -> {
                loadContracts();
                showSuccess("Signature enregistrée avec succès!");
            });

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Signer le Contrat");
            dialogStage.setScene(new Scene(root, 1000, 800));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            signatureController.setDialogStage(dialogStage);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du dialog de signature: " + e.getMessage());
        }
    }

    private void downloadContractPDF(Contrat contract) {
        // Vérifier si le contrat est complètement signé
        if (contract.getClientSignatureDate() == null || contract.getFreelancerSignatureDate() == null) {
            showError("Le contrat ne peut être exporté que s'il est complètement signé par le client et le freelancer");
            return;
        }

        showSuccess("Fonctionnalité d'export PDF en cours de développement pour le contrat #" + contract.getIdContract());
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        return label;
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

