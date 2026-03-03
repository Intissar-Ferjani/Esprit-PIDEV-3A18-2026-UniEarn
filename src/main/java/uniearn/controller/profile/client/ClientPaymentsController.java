package uniearn.controller.profile.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uniearn.model.entities.PaymentEscrow;
import uniearn.services.payment.EscrowPaymentService;
import uniearn.database.SessionManager;
import uniearn.services.users.client.ClientService;
import uniearn.model.entities.users.client.Client;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contrôleur pour la section Paiements du dashboard client
 */
public class ClientPaymentsController {

    @FXML
    private VBox paymentsContainer;

    @FXML
    private ComboBox<String> filterStatus;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<PaymentEscrow> paymentsTable;

    @FXML
    private TableColumn<PaymentEscrow, String> colContractId;

    @FXML
    private TableColumn<PaymentEscrow, BigDecimal> colAmount;

    @FXML
    private TableColumn<PaymentEscrow, String> colStatus;

    @FXML
    private TableColumn<PaymentEscrow, String> colDateCreation;

    @FXML
    private TableColumn<PaymentEscrow, String> colActions;

    @FXML
    private Label totalBlockedLabel;

    @FXML
    private Label totalReleasedLabel;

    private final EscrowPaymentService escrowService = new EscrowPaymentService();
    private final ObservableList<PaymentEscrow> paymentsData = FXCollections.observableArrayList();
    private final ClientService clientService = new ClientService();
    private int clientId;

    @FXML
    public void initialize() {
        resolveClientId();
        setupTable();
        setupFilters();
        loadPayments();
    }

    private void resolveClientId() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            System.err.println("⚠ Aucun utilisateur connecté - paiements client indisponibles");
            return;
        }

        int userId = SessionManager.getInstance().getCurrentUserId();
        try {
            Client client = clientService.getClientById(userId);
            if (client != null && client.getIdClient() > 0) {
                clientId = client.getIdClient();
                System.out.println("DEBUG: client userID=" + userId + " -> idClient=" + clientId);
            } else {
                System.err.println("⚠ Impossible de retrouver l'idClient pour l'utilisateur " + userId);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configure les colonnes du tableau
     */
    private void setupTable() {
        if (paymentsTable != null) {
            // Colonne ID Contrat
            colContractId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                    String.valueOf(cellData.getValue().getContractId())
                )
            );

            // Colonne Montant
            colAmount.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAmount())
            );
            colAmount.setCellFactory(column -> new TableCell<PaymentEscrow, BigDecimal>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : String.format("%.2f TND", item.doubleValue()));
                }
            });

            // Colonne Statut
            colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus())
            );
            colStatus.setCellFactory(column -> new TableCell<PaymentEscrow, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle(null);
                    } else {
                        setText(translateStatus(item));
                        setStyle(getStatusStyle(item));
                    }
                }
            });

            // Colonne Date
            colDateCreation.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getDateCreation() != null ?
                        cellData.getValue().getDateCreation()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) :
                        "N/A"
                )
            );

            // Colonne Actions
            colActions.setCellFactory(column -> new TableCell<PaymentEscrow, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null) {
                        setGraphic(null);
                    } else {
                        PaymentEscrow escrow = getTableRow().getItem();
                        if (escrow != null) {
                            setGraphic(createActionButtons(escrow));
                        }
                    }
                }
            });

            paymentsTable.setItems(paymentsData);
        }
    }

    /**
     * Configure les filtres
     */
    private void setupFilters() {
        if (filterStatus != null) {
            ObservableList<String> statuses = FXCollections.observableArrayList(
                "Tous",
                "Bloqué",
                "Livré",
                "Remboursé"
            );
            filterStatus.setItems(statuses);
            filterStatus.setValue("Tous");

            filterStatus.setOnAction(e -> loadPayments());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> loadPayments());
        }
    }

    /**
     * Charge les paiements du client
     */
    public void loadPayments() {
        if (clientId <= 0) {
            System.err.println("⚠ Paiements client non disponibles - idClient absent");
            javafx.application.Platform.runLater(paymentsData::clear);
            return;
        }
        new Thread(() -> {
            try {
                List<PaymentEscrow> payments = escrowService.getEscrowsByClient(clientId);

                // Filtre par statut
                String selectedStatus = filterStatus != null ? filterStatus.getValue() : "Tous";
                if (!selectedStatus.equals("Tous")) {
                    String statusFilter = translateStatusToEnglish(selectedStatus);
                    payments = payments.stream()
                        .filter(p -> p.getStatus().equals(statusFilter))
                        .toList();
                }

                // Filtre par recherche
                String searchText = searchField != null ? searchField.getText().toLowerCase() : "";
                if (!searchText.isEmpty()) {
                    final List<PaymentEscrow> finalPayments = payments;
                    payments = finalPayments.stream()
                        .filter(p -> String.valueOf(p.getContractId()).contains(searchText) ||
                                    p.getAmount().toString().contains(searchText))
                        .toList();
                }

                final List<PaymentEscrow> finalPayments = payments;
                javafx.application.Platform.runLater(() -> {
                    paymentsData.clear();
                    paymentsData.addAll(finalPayments);
                    updateStatistics(finalPayments);
                });
            } catch (Exception e) {
                System.err.println("❌ Erreur lors du chargement des paiements: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Crée les boutons d'action pour chaque ligne
     */
    private HBox createActionButtons(PaymentEscrow escrow) {
        HBox hbox = new HBox();
        hbox.setSpacing(10);
        hbox.setPadding(new Insets(5));

        Button btnDetails = new Button("Détails");
        btnDetails.setStyle("-fx-padding: 5px 15px; -fx-font-size: 12px;");
        btnDetails.setOnAction(e -> showPaymentDetails(escrow));

        hbox.getChildren().add(btnDetails);
        return hbox;
    }

    /**
     * Affiche les détails d'un paiement
     */
    private void showPaymentDetails(PaymentEscrow escrow) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du paiement");
        alert.setHeaderText("Escrow #" + escrow.getId());

        VBox content = new VBox();
        content.setSpacing(10);
        content.setPadding(new Insets(10));

        content.getChildren().addAll(
            new Label("ID Contrat: " + escrow.getContractId()),
            new Label("Montant: " + String.format("%.2f TND", escrow.getAmount().doubleValue())),
            new Label("Statut: " + translateStatus(escrow.getStatus())),
            new Label("Date de création: " + (escrow.getDateCreation() != null ?
                escrow.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A")),
            new Separator(),
            new Label("Notes: " + (escrow.getNotes() != null ? escrow.getNotes() : "Aucune note"))
        );

        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    /**
     * Met à jour les statistiques
     */
    private void updateStatistics(List<PaymentEscrow> payments) {
        // Montants bloqués = uniquement PENDING (en attente)
        BigDecimal totalBlocked = payments.stream()
            .filter(p -> p.getStatus().equals("PENDING"))
            .map(PaymentEscrow::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Montants libérés = COMPLETED + RELEASED (projet livré ou montant transféré)
        BigDecimal totalReleased = payments.stream()
            .filter(p -> p.getStatus().equals("COMPLETED") || p.getStatus().equals("RELEASED"))
            .map(PaymentEscrow::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalBlockedLabel != null) {
            totalBlockedLabel.setText(String.format("%.2f TND", totalBlocked.doubleValue()));
        }
        if (totalReleasedLabel != null) {
            totalReleasedLabel.setText(String.format("%.2f TND", totalReleased.doubleValue()));
        }
    }

    /**
     * Traduit le statut en français
     */
    private String translateStatus(String status) {
        return switch (status) {
            case "PENDING" -> "Bloqué";
            case "COMPLETED" -> "Livré";
            case "RELEASED" -> "Livré";
            case "REFUNDED" -> "Remboursé";
            default -> status;
        };
    }

    /**
     * Traduit le statut en anglais
     */
    private String translateStatusToEnglish(String status) {
        return switch (status) {
            case "Bloqué" -> "PENDING";
            case "Livré" -> "RELEASED";
            case "Remboursé" -> "REFUNDED";
            default -> status;
        };
    }

    /**
     * Retourne le style CSS pour un statut
     */
    private String getStatusStyle(String status) {
        return switch (status) {
            case "PENDING" -> "-fx-text-fill: #FF9800; -fx-font-weight: bold;";
            case "COMPLETED", "RELEASED" -> "-fx-text-fill: #4CAF50; -fx-font-weight: bold;";
            case "REFUNDED" -> "-fx-text-fill: #F44336; -fx-font-weight: bold;";
            default -> "";
        };
    }
}

