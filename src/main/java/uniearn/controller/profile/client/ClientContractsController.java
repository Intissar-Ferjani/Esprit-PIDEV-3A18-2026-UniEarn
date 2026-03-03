package uniearn.controller.profile.client;

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
import javafx.stage.Modality;
import javafx.stage.Stage;
import uniearn.model.entities.users.client.Client;
import uniearn.model.entities.contracts.Contrat;
import uniearn.model.entities.PaymentEscrow;
import uniearn.services.contracts.ContratService;
import uniearn.services.payment.EscrowPaymentService;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import uniearn.database.MyConnection;

/**
 * Contrôleur pour afficher les contrats du client dans son profil
 */
public class ClientContractsController {

    @FXML private VBox contractsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label noContractsLabel;
    @FXML private ScrollPane scrollPane;
    @FXML private Button btnNewContract;
    @FXML private Button btnRefresh;

    private TableView<Contrat> contractsTable;
    private TableColumn<Contrat, Integer> colId;
    private TableColumn<Contrat, String> colType;
    private TableColumn<Contrat, Double> colAmount;
    private TableColumn<Contrat, String> colStatus;
    private TableColumn<Contrat, String> colStartDate;
    private TableColumn<Contrat, Void> colActions;

    private Client currentClient;
    private final ContratService contratService = new ContratService();
    private ObservableList<Contrat> contractsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        createContractsTable();
        setupFilters();
        setupSearch();
    }

    private void createContractsTable() {
        // Créer le tableau
        contractsTable = new TableView<>();
        contractsTable.setPrefHeight(400);
        contractsTable.setStyle("-fx-font-size: 12;");

        // Colonne ID
        colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idContract"));
        colId.setPrefWidth(50);

        // Colonne Type
        colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("type"));
        colType.setPrefWidth(120);

        // Colonne Montant
        colAmount = new TableColumn<>("Montant (TND)");
        colAmount.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("amount"));
        colAmount.setPrefWidth(100);

        // Colonne Statut
        colStatus = new TableColumn<>("Statut");
        colStatus.setCellValueFactory(cellData -> {
            int status = cellData.getValue().getStatus();
            String statusStr = getStatusString(status);
            return new javafx.beans.property.SimpleStringProperty(statusStr);
        });
        colStatus.setPrefWidth(120);

        // Colonne Date Début
        colStartDate = new TableColumn<>("Date Début");
        colStartDate.setCellValueFactory(cellData -> {
            String date = cellData.getValue().getStartDate() != null ? cellData.getValue().getStartDate().toString() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(date);
        });
        colStartDate.setPrefWidth(100);

        // Colonne Actions
        colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(250);
        colActions.setCellFactory(param -> new javafx.scene.control.TableCell<Contrat, Void>() {
            private final HBox pane = new HBox(5);
            private final Button payBtn = new Button("💳 Payer");
            private final Button signBtn = new Button("✍ Signer");
            private final Button viewBtn = new Button("👁 Détails");
            private final Button deleteBtn = new Button("🗑 Supprimer");

            {
                payBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11; -fx-text-fill: white; -fx-background-color: #4CAF50;");
                payBtn.setOnAction(event -> openPaymentDialog(getTableView().getItems().get(getIndex())));

                signBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11;");
                signBtn.setOnAction(event -> openSignatureDialog(getTableView().getItems().get(getIndex())));

                viewBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11;");
                viewBtn.setOnAction(event -> showContractDetails(getTableView().getItems().get(getIndex())));

                deleteBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11;");
                deleteBtn.setOnAction(event -> deleteContract(getTableView().getItems().get(getIndex())));

                pane.getChildren().addAll(payBtn, signBtn, viewBtn, deleteBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView() == null || getIndex() < 0) {
                    setGraphic(null);
                } else {
                    // Afficher le bouton Payer seulement pour les contrats Actif (status = 3 - signés par les deux)
                    Contrat currentContract = getTableView().getItems().get(getIndex());
                    payBtn.setVisible(currentContract.getStatus() == 3);
                    payBtn.setManaged(currentContract.getStatus() == 3);

                    setGraphic(pane);
                }
            }
        });

        contractsTable.getColumns().addAll(colId, colType, colAmount, colStatus, colStartDate, colActions);
        contractsTable.setItems(contractsList);

        // Remplacer le contenu du scrollPane
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

    public void setClientData(Client client) {
        this.currentClient = client;
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
            if (currentClient != null) {
                loadContracts();
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (currentClient != null) {
                loadContracts();
            }
        });
    }

    private void loadContracts() {
        if (currentClient == null) return;

        try {
            // Charger les contrats du client
            List<Contrat> contracts = contratService.getAllContrats();
            // Filtrer par client ID
            contracts = contracts.stream()
                    .filter(c -> c.getClientID() == currentClient.getIdClient())
                    .toList();

            // Appliquer les filtres
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

    private void downloadContract(Contrat contract) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Télécharger le Contrat");
        alert.setHeaderText("Téléchargement");
        alert.setContentText("La fonctionnalité de téléchargement sera bientôt disponible pour le contrat #" + contract.getIdContract());
        alert.showAndWait();
    }

    private void deleteContract(Contrat contract) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le Contrat");
        confirm.setHeaderText("Êtes-vous sûr ?");
        confirm.setContentText("Cette action supprimera définitivement le contrat #" + contract.getIdContract());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    contratService.deleteContrat(contract.getIdContract());
                    loadContracts();
                    showSuccess("Le contrat a été supprimé avec succès");
                } catch (Exception e) {
                    showError("Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
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

    @FXML
    private void handleNewContract() {
        try {
            if (currentClient == null) {
                showError("Erreur: Données client non disponibles");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/client_contract_dialog.fxml"));
            Parent root = loader.load();

            uniearn.controller.contracts.client.ClientContractDialogController controller = loader.getController();

            // Passer l'ID client (pas l'ID utilisateur!)
            int clientID = currentClient.getIdClient();
            System.out.println("DEBUG: Passage de clientID = " + clientID + " (" + currentClient.getName() + ")");
            controller.setClientID(clientID);
            controller.setOnContractCreated(contract -> loadContracts());

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Créer un nouveau contrat");
            dialogStage.setScene(new Scene(root, 700, 600));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du dialog de création: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadContracts();
    }

    private void openSignatureDialog(Contrat contract) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contract_signature.fxml"));
            Parent root = loader.load();

            uniearn.controller.contracts.contrat.ContractSignatureController signatureController = loader.getController();
            signatureController.setContract(contract);
            signatureController.setContratService(contratService);
            signatureController.setUserType("CLIENT"); // Indiquer que c'est un client
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

    private void openPaymentDialog(Contrat contract) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Effectuer le Paiement");
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Informations du contrat
        Label titleLabel = new Label("💳 Paiement du Contrat");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label typeLabel = new Label("Type: " + contract.getType());
        Label amountLabel = new Label("Montant: " + String.format("%.2f", contract.getAmount()) + " TND");
        Label idLabel = new Label("Contrat ID: " + contract.getIdContract());

        content.getChildren().addAll(
            titleLabel,
            new Separator(),
            typeLabel,
            amountLabel,
            idLabel,
            new Separator()
        );

        // Sélectionner le compte bancaire
        Label bankLabel = new Label("Sélectionner le compte bancaire:");
        bankLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> bankCombo = new ComboBox<>();
        bankCombo.setItems(FXCollections.observableArrayList(
            "Compte bancaire - BNP Paribas (****2606)",
            "Ajouter un nouveau compte"
        ));
        bankCombo.setValue("Compte bancaire - BNP Paribas (****2606)");

        content.getChildren().addAll(bankLabel, bankCombo);

        // Message de confirmation
        Label confirmLabel = new Label("✅ En cliquant \"Confirmer\", vous autorisez le paiement de " +
            String.format("%.2f", contract.getAmount()) + " TND");
        confirmLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 11;");
        confirmLabel.setWrapText(true);

        content.getChildren().add(confirmLabel);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Simuler le paiement
            simulatePayment(contract);
        }
    }

    private void simulatePayment(Contrat contract) {
        if (contract.getFreelancerID() <= 0) {
            showError("❌ Erreur: Aucun freelancer n'est associé à ce contrat.\n\nVeuillez contacter l'administrateur.");
            return;
        }

        Integer freelancerDbId = resolveFreelancerForeignKey(contract.getFreelancerID());
        if (freelancerDbId == null) {
            showError("❌ Erreur: Impossible de retrouver le freelancer associé.\nVeuillez contacter l'administrateur.");
            return;
        }

        EscrowPaymentService escrowService = new EscrowPaymentService();
        try {
            PaymentEscrow escrow = escrowService.createEscrow(
                contract.getIdContract(),
                currentClient.getIdClient(),
                freelancerDbId,
                new java.math.BigDecimal(contract.getAmount())
            );

            if (escrow != null) {
                showSuccess("✅ Paiement effectué avec succès!\n\nMontant bloqué chez l'administrateur en attente de validation.");
                loadContracts();
            } else {
                showError("Impossible d'effectuer le paiement. Veuillez réessayer.");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création de l'escrow: " + e.getMessage());
            e.printStackTrace();
            showError("❌ Erreur lors du paiement:\n\n" + e.getMessage());
        }
    }

    private Integer resolveFreelancerForeignKey(int storedIdentifier) {
        String sql = "SELECT idFreelancer FROM freelancer WHERE idFreelancer = ? OR idUser = ? LIMIT 1";
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, storedIdentifier);
                ps.setInt(2, storedIdentifier);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("idFreelancer");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur résolution freelancer: " + e.getMessage());
        }
        return null;
    }
}


