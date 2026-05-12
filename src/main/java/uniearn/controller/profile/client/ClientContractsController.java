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
    private TableColumn<Contrat, String> colAmount;
    private TableColumn<Contrat, String> colStatus;
    private TableColumn<Contrat, String> colStartDate;
    private TableColumn<Contrat, Void> colActions;

    private Client currentClient;
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
        // Créer le tableau
        contractsTable = new TableView<>();
        contractsTable.setPrefHeight(450);
        contractsTable.getStyleClass().add("premium-table");
        
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
        colId = new TableColumn<>("#");
        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idContract"));
        colId.setPrefWidth(50);
        colId.setStyle("-fx-alignment: CENTER; -fx-text-fill: #64748b;");

        // Colonne Titre (Title)
        colType = new TableColumn<>("Title");
        colType.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("title"));
        colType.setPrefWidth(200);
        colType.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Colonne Freelancer (Nouveau)
        TableColumn<Contrat, String> colFreelancerName = new TableColumn<>("Freelancer");
        colFreelancerName.setCellValueFactory(cellData -> {
            String name = dataLoader.getFreelancerNameByFreelancerId(cellData.getValue().getFreelancerID());
            return new javafx.beans.property.SimpleStringProperty(name);
        });
        colFreelancerName.setPrefWidth(150);
        colFreelancerName.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #64748b;");

        // Colonne Montant (Amount)
        colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(cellData -> {
            double amount = cellData.getValue().getAmount();
            return new javafx.beans.property.SimpleStringProperty(String.format("$%.2f", amount));
        });
        colAmount.setPrefWidth(120);
        colAmount.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Colonne Statut (Status avec Badges)
        colStatus = new TableColumn<>("Status");
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
                    
                     // Couleurs intenses basées sur Symfony
                     switch (statusStr.toLowerCase()) {
                         case "actif", "signed" -> badge.setStyle(badge.getStyle() + "-fx-background-color: #c8e6c9; -fx-text-fill: #1b5e20;"); // Green intense
                         case "completed" -> badge.setStyle(badge.getStyle() + "-fx-background-color: #ffcdd2; -fx-text-fill: #b71c1c;"); // Red intense
                         case "annulé" -> badge.setStyle(badge.getStyle() + "-fx-background-color: #eceff1; -fx-text-fill: #455a64;"); // Gray intense
                         default -> badge.setStyle(badge.getStyle() + "-fx-background-color: #ffe0b2; -fx-text-fill: #e65100;"); // Orange intense (Pending)
                     }
                    
                    HBox container = new HBox(badge);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                }
            }
        });

         // Colonne Actions (Blue "View" Button + Delete Button)
         colActions = new TableColumn<>("Actions");
         colActions.setPrefWidth(200);
         colActions.setCellFactory(param -> new javafx.scene.control.TableCell<Contrat, Void>() {
             private final Button viewBtn = new Button("View");
             private final Button deleteBtn = new Button("🗑️");
             private final HBox pane = new HBox(8, viewBtn, deleteBtn);
             {
                 pane.setAlignment(Pos.CENTER);
                  viewBtn.setStyle(
                      "-fx-background-color: #42a5f5; " +
                      "-fx-text-fill: white; " +
                      "-fx-font-weight: bold; " +
                      "-fx-background-radius: 5; " +
                      "-fx-padding: 6 20; " +
                      "-fx-cursor: hand;"
                  );
                  deleteBtn.setStyle(
                      "-fx-background-color: #ef5350; " +
                      "-fx-text-fill: white; " +
                      "-fx-font-weight: bold; " +
                      "-fx-background-radius: 5; " +
                      "-fx-padding: 6 10; " +
                      "-fx-cursor: hand;"
                  );
                 viewBtn.setOnAction(event -> openSignatureDialog(getTableView().getItems().get(getIndex())));
                 deleteBtn.setOnAction(event -> deleteContract(getTableView().getItems().get(getIndex())));
             }

             @Override
             protected void updateItem(Void item, boolean empty) {
                 super.updateItem(item, empty);
                 if (empty) setGraphic(null);
                 else setGraphic(pane);
             }
         });

        contractsTable.getColumns().addAll(colId, colType, colFreelancerName, colAmount, colStatus, colActions);
        contractsTable.setItems(contractsList);

        // Remplacer le contenu du scrollPane
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
        if (currentClient == null) {
            System.err.println("❌ ERROR: ClientContractsController.loadContracts() - currentClient is null!");
            return;
        }

        try {
            System.out.println("🔍 DIAGNOSTIC: Starting loadContracts for User: " + currentClient.getName() + " (ID User: " + currentClient.getIdUser() + ")");
            
            // FIX: Résoudre l'idClient réel à partir de l'idUser pour garantir la correspondance avec le Web
            int resolvedClientId = dataLoader.getClientIdByUserId(currentClient.getIdUser());
            System.out.println("🔍 DIAGNOSTIC: Resolved idClient from DB: " + resolvedClientId);
            
            if (resolvedClientId <= 0) {
                System.err.println("⚠ WARNING: Could not resolve a valid idClient for idUser=" + currentClient.getIdUser() + ". Falling back to object ID: " + currentClient.getIdClient());
                resolvedClientId = currentClient.getIdClient();
            }
            
            if (resolvedClientId <= 0) {
                System.err.println("❌ ERROR: No valid Client ID found. Cannot load contracts.");
                contractsList.clear();
                return;
            }

            // Charger directement les contrats du client
            System.out.println("📡 DB FETCH: Executing getContratsByClient(" + resolvedClientId + ")...");
            List<Contrat> contracts = contratService.getContratsByClient(resolvedClientId);
            
            System.out.println("✅ DB RESULT: " + contracts.size() + " contracts found in database for client #" + resolvedClientId);
            
            if (contracts.isEmpty()) {
                // Check if any contracts exist AT ALL in the table for debugging
                List<Contrat> all = contratService.getAllContrats();
                System.out.println("🔍 DEBUG: Total contracts in 'contract' table (all users): " + all.size());
                if (!all.isEmpty()) {
                    System.out.println("🔍 DEBUG: First contract in DB has idClient=" + all.get(0).getClientID());
                }
            }

            // Appliquer les filtres de recherche et statut (en mémoire)
            String searchText = (searchField != null && searchField.getText() != null) ? searchField.getText().toLowerCase() : "";
            String statusFilterValue = (statusFilter != null && statusFilter.getValue() != null) ? statusFilter.getValue() : "Tous les statuts";

            List<Contrat> filtered = contracts.stream()
                    .filter(c -> filterByStatus(c, statusFilterValue))
                    .filter(c -> filterBySearch(c, searchText))
                    .toList();

            contractsList.setAll(filtered);
            
            // Gérer l'affichage du label "Aucun contrat"
            if (noContractsLabel != null) {
                if (filtered.isEmpty()) {
                    noContractsLabel.setText(contracts.isEmpty() ? "Vous n'avez pas encore de contrats" : "Aucun contrat ne correspond à votre recherche");
                    noContractsLabel.setVisible(true);
                    noContractsLabel.setManaged(true);
                } else {
                    noContractsLabel.setVisible(false);
                    noContractsLabel.setManaged(false);
                }
            }

        } catch (Exception e) {
            System.err.println("ERROR: Erreur lors du chargement des contrats: " + e.getMessage());
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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contract_template_selection.fxml"));
            Parent root = loader.load();

            uniearn.controller.contracts.client.ContractTemplateSelectionController controller = loader.getController();

            // Passer l'ID client
            int clientID = currentClient.getIdClient();
            controller.setClientID(clientID);
            controller.setOnContractCreated(() -> loadContracts());

            Stage dialogStage = new Stage();
            controller.setDialogStage(dialogStage); // Missing this line
            dialogStage.setTitle("Sélectionner un Template");
            dialogStage.setScene(new Scene(root, 950, 750));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture de la sélection de template: " + e.getMessage());
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


