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
    private final uniearn.services.DataLoaderService dataLoader = new uniearn.services.DataLoaderService();
    private final ObservableList<Contrat> contractsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        createContractsTable();
        setupFilters();
        setupSearch();
    }

    private void createContractsTable() {
        contractsTable = new TableView<>();
        contractsTable.setPrefHeight(450);
        
        // Styling CSS direct pour correspondre au Web (En-tête bleu)
        contractsTable.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-border-radius: 10; " +
            "-fx-border-color: #e2e8f0; " +
            "-fx-font-family: 'Segoe UI', system-ui; " +
            "-fx-font-size: 13;"
        );

        // Colonne ID (#)
        TableColumn<Contrat, Integer> colId = new TableColumn<>("#");
        colId.setCellValueFactory(new PropertyValueFactory<>("idContract"));
        colId.setPrefWidth(50);
        colId.setStyle("-fx-alignment: CENTER; -fx-text-fill: #64748b;");

        // Colonne Titre (Title)
        TableColumn<Contrat, String> colTitle = new TableColumn<>("Title");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTitle.setPrefWidth(200);
        colTitle.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Colonne Client (Name)
        TableColumn<Contrat, String> colClientName = new TableColumn<>("Client");
        colClientName.setCellValueFactory(cellData -> {
            String name = dataLoader.getClientName(cellData.getValue().getClientID());
            return new javafx.beans.property.SimpleStringProperty(name);
        });
        colClientName.setPrefWidth(150);
        colClientName.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #64748b;");

        // Colonne Montant (Amount)
        TableColumn<Contrat, String> colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(cellData -> {
            double amount = cellData.getValue().getAmount();
            return new javafx.beans.property.SimpleStringProperty(String.format("$%.2f", amount));
        });
        colAmount.setPrefWidth(120);
        colAmount.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Colonne Statut (Status avec Badges)
        TableColumn<Contrat, String> colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(130);
        colStatus.setCellFactory(column -> new TableCell<Contrat, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Contrat c = (Contrat) getTableRow().getItem();
                    String statusStr = getStatusString(c.getStatus());
                    
                    Label badge = new Label(statusStr.toUpperCase());
                    badge.setPadding(new Insets(4, 12, 4, 12));
                    badge.setStyle("-fx-font-size: 10; -fx-font-weight: bold; -fx-background-radius: 20;");
                    
                    switch (statusStr.toLowerCase()) {
                        case "actif", "signed" -> badge.setStyle(badge.getStyle() + "-fx-background-color: #d1fae5; -fx-text-fill: #059669;");
                        case "completed" -> badge.setStyle(badge.getStyle() + "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;");
                        case "annulé" -> badge.setStyle(badge.getStyle() + "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;");
                        default -> badge.setStyle(badge.getStyle() + "-fx-background-color: #fef3c7; -fx-text-fill: #d97706;");
                    }
                    
                    HBox container = new HBox(badge);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                }
            }
        });

        // Colonne Actions (Blue View Button)
        TableColumn<Contrat, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<Contrat, Void>() {
            private final Button viewBtn = new Button("View");
            private final HBox pane = new HBox(viewBtn);
            {
                pane.setAlignment(Pos.CENTER);
                viewBtn.setStyle(
                    "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 6 20; -fx-cursor: hand;"
                );
                viewBtn.setOnAction(event -> openSignatureDialog(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(pane);
            }
        });

        contractsTable.getColumns().addAll(colId, colTitle, colClientName, colAmount, colStatus, colActions);
        contractsTable.setItems(contractsList);

        scrollPane.setContent(contractsTable);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
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
            System.out.println("DEBUG: loadContracts() - Freelancer idUser=" + currentFreelancer.getIdUser());
            List<Contrat> contracts = contratService.getAllContrats();
            System.out.println("DEBUG: Total contracts in DB: " + contracts.size());

            // Get the actual idFreelancer from the freelancer table (not idUser)
            uniearn.services.DataLoaderService dataLoader = new uniearn.services.DataLoaderService();
            Integer idFreelancer = dataLoader.getFreelancerIdByUserId(currentFreelancer.getIdUser());
            System.out.println("DEBUG: Resolved idFreelancer = " + idFreelancer);

            if (idFreelancer == null) {
                System.err.println("ERROR: No freelancer entry found for idUser=" + currentFreelancer.getIdUser());
                contractsList.clear();
                noContractsLabel.setText("Aucun contrat trouvé");
                noContractsLabel.setVisible(true);
                return;
            }

            final int freelancerId = idFreelancer;
            contracts = contracts.stream()
                    .peek(c -> System.out.println("DEBUG: Checking contract ID=" + c.getIdContract() +
                           " freelancerID=" + c.getFreelancerID() + " expected=" + freelancerId))
                    .filter(c -> c.getFreelancerID() == freelancerId)
                    .toList();

            System.out.println("DEBUG: Filtered contracts for freelancer: " + contracts.size());

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
        // Open the same Symfony-style view as "Signer"
        openSignatureDialog(contract);
    }

    private void openSignatureDialog(Contrat contract) {
        try {
            // Fetch full contract data including images before showing/signing
            Contrat fullContract = contratService.getContratById(contract.getIdContract());
            if (fullContract != null) {
                contract = fullContract;
            }

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

