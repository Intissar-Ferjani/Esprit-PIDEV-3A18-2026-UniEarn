package uniearn.controller.profile.freelancer;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import uniearn.model.entities.contracts.Contrat;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.services.contracts.ContratService;
import uniearn.services.users.freelancer.FreelancerService;
import uniearn.database.SessionManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller pour la page des paiements Stripe côté Freelancer
 * Affiche les fonds bloqués en escrow et les fonds reçus
 *
 * ⚠️ NOUVELLE VERSION - Utilise Stripe pour les paiements
 */
public class FreelancerPaymentsController {

    @FXML private Label statBlockedCount;
    @FXML private Label statReceivedCount;
    @FXML private Label statTotalBlocked;
    @FXML private Label statTotalReceived;

    @FXML private TableView<Contrat> blockedTable;
    @FXML private TableColumn<Contrat, Integer> blockedColId;
    @FXML private TableColumn<Contrat, String> blockedColTitle;
    @FXML private TableColumn<Contrat, String> blockedColClient;
    @FXML private TableColumn<Contrat, Double> blockedColAmount;
    @FXML private TableColumn<Contrat, String> blockedColStatus;
    @FXML private TableColumn<Contrat, Void> blockedColActions;

    @FXML private TableView<Contrat> receivedTable;
    @FXML private TableColumn<Contrat, Integer> receivedColId;
    @FXML private TableColumn<Contrat, String> receivedColTitle;
    @FXML private TableColumn<Contrat, String> receivedColClient;
    @FXML private TableColumn<Contrat, Double> receivedColAmount;
    @FXML private TableColumn<Contrat, String> receivedColStatus;
    @FXML private TableColumn<Contrat, Void> receivedColActions;

    private final ContratService contratService = new ContratService();
    private final FreelancerService freelancerService = new FreelancerService();

    private Freelancer currentFreelancer;
    private ObservableList<Contrat> blockedList = FXCollections.observableArrayList();
    private ObservableList<Contrat> receivedList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadCurrentFreelancer();
        resolveInjectedColumns();
        setupTables();
        loadPayments();
    }

    private void loadCurrentFreelancer() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            showError("Erreur", "Vous devez être connecté");
            return;
        }

        int userId = SessionManager.getInstance().getCurrentUserId();
        currentFreelancer = freelancerService.getFreelancerById(userId);

        if (currentFreelancer == null) {
            showError("Erreur", "Freelancer introuvable");
        }
    }

    private void setupTables() {
        if (blockedTable == null || receivedTable == null) {
            System.err.println("ERROR: Freelancer payment tables are not injected from FXML");
            return;
        }

        // Table "Bloqué en Escrow"
        blockedColId.setCellValueFactory(data ->
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getIdContract()));
        blockedColTitle.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getTitle() != null ? data.getValue().getTitle() : data.getValue().getType()));
        blockedColClient.setCellValueFactory(data ->
            new SimpleStringProperty(resolveClientName(data.getValue().getClientID())));
        blockedColAmount.setCellValueFactory(data ->
            new SimpleDoubleProperty(data.getValue().getAmount()).asObject());
        blockedColStatus.setCellValueFactory(data ->
            new SimpleStringProperty(getStatusBadge(data.getValue().getStatus())));

        // Colonne Actions pour "Bloqué"
        blockedColActions.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("👁 View");

            {
                viewButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; " +
                        "-fx-font-weight: bold; -fx-cursor: hand; " +
                        "-fx-padding: 6 14; -fx-border-radius: 8; -fx-background-radius: 8;");

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
                    setGraphic(viewButton);
                }
            }
        });

        // Table "Reçu"
        receivedColId.setCellValueFactory(data ->
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getIdContract()));
        receivedColTitle.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getTitle() != null ? data.getValue().getTitle() : data.getValue().getType()));
        receivedColClient.setCellValueFactory(data ->
            new SimpleStringProperty(resolveClientName(data.getValue().getClientID())));
        receivedColAmount.setCellValueFactory(data ->
            new SimpleDoubleProperty(data.getValue().getAmount()).asObject());
        receivedColStatus.setCellValueFactory(data ->
            new SimpleStringProperty(getStatusBadge(data.getValue().getStatus())));

        // Colonne Actions pour "Reçu"
        receivedColActions.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("👁 View");

            {
                viewButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; " +
                        "-fx-font-weight: bold; -fx-cursor: hand; " +
                        "-fx-padding: 6 14; -fx-border-radius: 8; -fx-background-radius: 8;");

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
                    setGraphic(viewButton);
                }
            }
        });

        blockedTable.setItems(blockedList);
        receivedTable.setItems(receivedList);
    }

    @SuppressWarnings("unchecked")
    private void resolveInjectedColumns() {
        if (blockedTable != null) {
            if (blockedColId == null && blockedTable.getColumns().size() > 0) blockedColId = (TableColumn<Contrat, Integer>) blockedTable.getColumns().get(0);
            if (blockedColTitle == null && blockedTable.getColumns().size() > 1) blockedColTitle = (TableColumn<Contrat, String>) blockedTable.getColumns().get(1);
            if (blockedColClient == null && blockedTable.getColumns().size() > 2) blockedColClient = (TableColumn<Contrat, String>) blockedTable.getColumns().get(2);
            if (blockedColAmount == null && blockedTable.getColumns().size() > 3) blockedColAmount = (TableColumn<Contrat, Double>) blockedTable.getColumns().get(3);
            if (blockedColStatus == null && blockedTable.getColumns().size() > 4) blockedColStatus = (TableColumn<Contrat, String>) blockedTable.getColumns().get(4);
            if (blockedColActions == null && blockedTable.getColumns().size() > 5) blockedColActions = (TableColumn<Contrat, Void>) blockedTable.getColumns().get(5);
        }

        if (receivedTable != null) {
            if (receivedColId == null && receivedTable.getColumns().size() > 0) receivedColId = (TableColumn<Contrat, Integer>) receivedTable.getColumns().get(0);
            if (receivedColTitle == null && receivedTable.getColumns().size() > 1) receivedColTitle = (TableColumn<Contrat, String>) receivedTable.getColumns().get(1);
            if (receivedColClient == null && receivedTable.getColumns().size() > 2) receivedColClient = (TableColumn<Contrat, String>) receivedTable.getColumns().get(2);
            if (receivedColAmount == null && receivedTable.getColumns().size() > 3) receivedColAmount = (TableColumn<Contrat, Double>) receivedTable.getColumns().get(3);
            if (receivedColStatus == null && receivedTable.getColumns().size() > 4) receivedColStatus = (TableColumn<Contrat, String>) receivedTable.getColumns().get(4);
            if (receivedColActions == null && receivedTable.getColumns().size() > 5) receivedColActions = (TableColumn<Contrat, Void>) receivedTable.getColumns().get(5);
        }
    }

    private void loadPayments() {
        if (currentFreelancer == null) return;

        List<Contrat> allContracts = contratService.getAllContrats();

        // Filtrer par freelancer
        List<Contrat> freelancerContracts = allContracts.stream()
                .filter(c -> c.getFreelancerID() == currentFreelancer.getIdFreelancer())
                .collect(Collectors.toList());

        // Séparer en "Bloqué" (status = 4 = funded) et "Reçu" (status = 5 ou 6)
        List<Contrat> blocked = freelancerContracts.stream()
                .filter(c -> c.getStatus() == 4) // funded
                .collect(Collectors.toList());

        List<Contrat> received = freelancerContracts.stream()
                .filter(c -> c.getStatus() >= 5) // released, completed
                .collect(Collectors.toList());

        blockedList.setAll(blocked);
        receivedList.setAll(received);

        updateStats(blocked, received);
    }

    private void updateStats(List<Contrat> blocked, List<Contrat> received) {
        if (statBlockedCount != null) statBlockedCount.setText(String.valueOf(blocked.size()));
        if (statReceivedCount != null) statReceivedCount.setText(String.valueOf(received.size()));

        double totalBlocked = blocked.stream().mapToDouble(Contrat::getAmount).sum();
        double totalReceived = received.stream().mapToDouble(Contrat::getAmount).sum();

        if (statTotalBlocked != null) statTotalBlocked.setText(String.format("$%.2f", totalBlocked));
        if (statTotalReceived != null) statTotalReceived.setText(String.format("$%.2f", totalReceived));
    }

    @FXML
    private void handleRefresh() {
        loadPayments();
        showInfo("Actualisation", "Les données ont été actualisées");
    }

    private void viewContract(Contrat contract) {
        // TODO: Naviguer vers la page de détails du contrat
        showInfo("Détails du contrat", "Ouverture du contrat #" + contract.getIdContract());
    }

    private String getStatusBadge(int status) {
        return switch (status) {
            case 4 -> "🔒 En Séquestre";
            case 5, 6 -> "✅ Terminé";
            default -> "⏳ En attente";
        };
    }

    private String resolveClientName(int clientId) {
        // Simple résolution pour l'instant, on pourrait utiliser un cache ou DataLoaderService
        return "Client #" + clientId;
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
}

