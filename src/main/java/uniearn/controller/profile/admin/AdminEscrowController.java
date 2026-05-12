package uniearn.controller.profile.admin;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import uniearn.model.entities.contracts.Contrat;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.model.entities.BankAccount;
import uniearn.services.contracts.ContratService;
import uniearn.services.users.freelancer.FreelancerService;
import uniearn.services.BankAccountService;
import uniearn.database.SessionManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller Admin pour gérer les fonds en escrow (Stripe)
 * Permet de libérer les fonds vers les freelancers ou rembourser les clients
 */
public class AdminEscrowController {

    @FXML private Label statTotalEscrow;
    @FXML private Label statFundedCount;

    @FXML private TableView<Contrat> escrowTable;
    @FXML private TableColumn<Contrat, Integer> colId;
    @FXML private TableColumn<Contrat, String> colTitle;
    @FXML private TableColumn<Contrat, String> colClient;
    @FXML private TableColumn<Contrat, String> colFreelancer;
    @FXML private TableColumn<Contrat, Double> colAmount;
    @FXML private TableColumn<Contrat, String> colDate;
    @FXML private TableColumn<Contrat, Void> colActions;

    private final ContratService contratService = new ContratService();
    private final FreelancerService freelancerService = new FreelancerService();
    private final BankAccountService bankAccountService = new BankAccountService();

    private final ObservableList<Contrat> escrowList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (!checkAdminAccess()) {
            return;
        }
        resolveInjectedColumns();
        setupTable();
        loadEscrowContracts();
    }

    private boolean checkAdminAccess() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            showError("Accès refusé", "Vous devez être connecté");
            return false;
        }

        // Vérifier si l'utilisateur est admin
        if (SessionManager.getInstance().getCurrentUser() == null || SessionManager.getInstance().getCurrentUser().getRole() == null) {
            showError("Accès refusé", "Session invalide");
            return false;
        }

        String role = SessionManager.getInstance().getCurrentUser().getRole().toString();
        if (!"ADMIN".equals(role)) {
            showError("Accès refusé", "Cette page est réservée aux administrateurs");
            return false;
        }

        return true;
    }

    private void setupTable() {
        if (escrowTable == null) {
            System.err.println("ERROR: Admin escrow table is not injected from FXML");
            return;
        }

        colId.setCellValueFactory(data ->
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getIdContract()));

        colTitle.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getType()));

        colClient.setCellValueFactory(data ->
            new SimpleStringProperty("Client #" + data.getValue().getClientID()));

        colFreelancer.setCellValueFactory(data ->
            new SimpleStringProperty("Freelancer #" + data.getValue().getFreelancerID()));

        colAmount.setCellValueFactory(data ->
            new SimpleDoubleProperty(data.getValue().getAmount()).asObject());

        colDate.setCellValueFactory(data -> {
            if (data.getValue().getStartDate() != null) {
                return new SimpleStringProperty(
                    data.getValue().getStartDate().toLocalDateTime().toLocalDate().toString()
                );
            }
            return new SimpleStringProperty("—");
        });

        // Colonne Actions
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button releaseButton = new Button("✅ Libérer");
            private final Button refundButton = new Button("💰 Rembourser");
            private final Button viewButton = new Button("👁 Voir");

            {
                releaseButton.setStyle(
                    "-fx-background-color: #22c55e; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-cursor: hand; " +
                    "-fx-padding: 6 12; -fx-border-radius: 8; -fx-background-radius: 8;"
                );

                refundButton.setStyle(
                    "-fx-background-color: #ef4444; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-cursor: hand; " +
                    "-fx-padding: 6 12; -fx-border-radius: 8; -fx-background-radius: 8;"
                );

                viewButton.setStyle(
                    "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; " +
                    "-fx-font-weight: bold; -fx-cursor: hand; " +
                    "-fx-padding: 6 12; -fx-border-radius: 8; -fx-background-radius: 8;"
                );

                releaseButton.setOnAction(event -> {
                    Contrat contract = getTableView().getItems().get(getIndex());
                    handleReleaseFunds(contract);
                });

                refundButton.setOnAction(event -> {
                    Contrat contract = getTableView().getItems().get(getIndex());
                    handleRefund(contract);
                });

                viewButton.setOnAction(event -> {
                    Contrat contract = getTableView().getItems().get(getIndex());
                    viewContract(contract);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(8, releaseButton, refundButton, viewButton);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        escrowTable.setItems(escrowList);
    }

    @SuppressWarnings("unchecked")
    private void resolveInjectedColumns() {
        if (escrowTable != null) {
            if (colId == null && escrowTable.getColumns().size() > 0) colId = (TableColumn<Contrat, Integer>) escrowTable.getColumns().get(0);
            if (colTitle == null && escrowTable.getColumns().size() > 1) colTitle = (TableColumn<Contrat, String>) escrowTable.getColumns().get(1);
            if (colClient == null && escrowTable.getColumns().size() > 2) colClient = (TableColumn<Contrat, String>) escrowTable.getColumns().get(2);
            if (colFreelancer == null && escrowTable.getColumns().size() > 3) colFreelancer = (TableColumn<Contrat, String>) escrowTable.getColumns().get(3);
            if (colAmount == null && escrowTable.getColumns().size() > 4) colAmount = (TableColumn<Contrat, Double>) escrowTable.getColumns().get(4);
            if (colDate == null && escrowTable.getColumns().size() > 5) colDate = (TableColumn<Contrat, String>) escrowTable.getColumns().get(5);
            if (colActions == null && escrowTable.getColumns().size() > 6) colActions = (TableColumn<Contrat, Void>) escrowTable.getColumns().get(6);
        }
    }

    private void loadEscrowContracts() {
        List<Contrat> allContracts = contratService.getAllContrats();

        // Filtrer uniquement les contrats en escrow (status = 4 = funded)
        List<Contrat> funded = allContracts.stream()
                .filter(c -> c.getStatus() == 4)
                .collect(Collectors.toList());

        escrowList.setAll(funded);
        updateStats(funded);
    }

    private void updateStats(List<Contrat> funded) {
        if (statFundedCount != null) {
            statFundedCount.setText(String.valueOf(funded.size()));
        }

        double totalEscrow = funded.stream().mapToDouble(Contrat::getAmount).sum();
        if (statTotalEscrow != null) {
            statTotalEscrow.setText(String.format("$%.2f", totalEscrow));
        }
    }

    private void handleReleaseFunds(Contrat contract) {
        if (contract.getStatus() != 4) {
            showError("Erreur", "Ces fonds ne sont pas en séquestre ou ont déjà été libérés.");
            return;
        }

        // Vérifier que le freelancer existe
        Freelancer freelancer = freelancerService.getFreelancerById(contract.getFreelancerID());
        if (freelancer == null) {
            showError("Erreur", "Freelancer introuvable.");
            return;
        }

        // Récupérer le compte bancaire par défaut du freelancer
        BankAccount bankAccount = bankAccountService.getDefaultBankAccount(freelancer.getIdUser());
        if (bankAccount == null || bankAccount.getIban() == null || bankAccount.getIban().isEmpty()) {
            showError("Erreur", "Impossible de transférer : le Freelancer n'a pas configuré son RIB/IBAN.");
            return;
        }

        // Confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer la libération");
        confirmAlert.setHeaderText("Libérer les fonds vers le freelancer");
        confirmAlert.setContentText(String.format(
            "Contrat: %s\nMontant: $%.2f\nFreelancer: %s\nIBAN: %s\n\nConfirmer la libération ?",
            contract.getType(),
            contract.getAmount(),
            freelancer.getName(),
            maskIban(bankAccount.getIban())
        ));

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                processRelease(contract, bankAccount);
            }
        });
    }

    private void processRelease(Contrat contract, BankAccount bankAccount) {
        try {
            // TODO: Appeler Stripe Payouts API pour transférer l'argent
            // Pour l'instant, on simule

            // Mettre à jour le statut du contrat
            contract.setStatus(5); // 5 = released
            contratService.updateContrat(contract);

            showSuccess("Fonds libérés",
                String.format("✅ $%.2f ont été (virtuellement) transférés vers l'IBAN : %s",
                    contract.getAmount(),
                    maskIban(bankAccount.getIban())));

            // Recharger les données
            loadEscrowContracts();

        } catch (Exception e) {
            showError("Erreur", "Erreur lors de la libération des fonds: " + e.getMessage());
            System.err.println("❌ Erreur lors de la libération: " + e.getMessage());
        }
    }

    private void handleRefund(Contrat contract) {
        if (contract.getStatus() != 4) {
            showError("Erreur", "Ces fonds ne sont pas en séquestre.");
            return;
        }

        // Confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer le remboursement");
        confirmAlert.setHeaderText("Rembourser le client");
        confirmAlert.setContentText(String.format(
            "Contrat: %s\nMontant: $%.2f\nClient: Client #%d\n\nConfirmer le remboursement ?",
            contract.getType(),
            contract.getAmount(),
            contract.getClientID()
        ));

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                processRefund(contract);
            }
        });
    }

    private void processRefund(Contrat contract) {
        try {
            // TODO: Appeler Stripe Refunds API pour rembourser
            // Pour l'instant, on simule

            // Mettre à jour le statut du contrat
            contract.setStatus(7); // 7 = cancelled/refunded
            contratService.updateContrat(contract);

            showSuccess("Remboursement effectué",
                String.format("✅ $%.2f ont été remboursés au client #%d",
                    contract.getAmount(),
                    contract.getClientID()));

            // Recharger les données
            loadEscrowContracts();

        } catch (Exception e) {
            showError("Erreur", "Erreur lors du remboursement: " + e.getMessage());
            System.err.println("❌ Erreur lors du remboursement: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadEscrowContracts();
        showInfo("Actualisation", "Les données ont été actualisées");
    }

    private void viewContract(Contrat contract) {
        // TODO: Naviguer vers la page de détails du contrat
        showInfo("Détails du contrat", "Ouverture du contrat #" + contract.getIdContract());
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Masque un IBAN pour n'afficher que le début et la fin
     */
    private String maskIban(String iban) {
        if (iban == null || iban.length() < 8) {
            return iban;
        }
        String start = iban.substring(0, 4);
        String end = iban.substring(iban.length() - 4);
        return start + "****" + end;
    }
}

