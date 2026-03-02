package uniearn.controller.profile.freelancer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import uniearn.model.entities.PaymentEscrow;
import uniearn.services.payment.EscrowPaymentService;
import uniearn.database.SessionManager;
import uniearn.services.users.freelancer.FreelancerService;
import uniearn.model.entities.users.freelancer.Freelancer;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contrôleur pour la section Paiements du profile freelancer
 */
public class FreelancerPaymentsController {

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
    private Label totalPendingLabel;

    @FXML
    private Label totalAvailableLabel;

    private final EscrowPaymentService escrowService = new EscrowPaymentService();
    private final ObservableList<PaymentEscrow> paymentsData = FXCollections.observableArrayList();
    private final FreelancerService freelancerService = new FreelancerService();
    private int freelancerId;

    @FXML
    public void initialize() {
        resolveFreelancerId();
        setupTable();
        setupFilters();
        loadPayments();
    }

    private void resolveFreelancerId() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            System.err.println("⚠ Aucun utilisateur connecté - impossible de charger les paiements freelancer");
            return;
        }

        int userId = SessionManager.getInstance().getCurrentUserId();
        try {
            Freelancer freelancer = freelancerService.getFreelancerById(userId);
            if (freelancer != null && freelancer.getIdFreelancer() > 0) {
                freelancerId = freelancer.getIdFreelancer();
                System.out.println("DEBUG: freelancer userID=" + userId + " -> idFreelancer=" + freelancerId);
            } else {
                System.err.println("⚠ Impossible de retrouver l'idFreelancer pour l'utilisateur " + userId);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du freelancer: " + e.getMessage());
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
                "Libéré",
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
     * Charge les paiements du freelancer
     */
    public void loadPayments() {
        if (freelancerId <= 0) {
            System.err.println("⚠ Paiements freelances non disponibles - idFreelancer absent");
            javafx.application.Platform.runLater(paymentsData::clear);
            return;
        }
        new Thread(() -> {
            try {
                List<PaymentEscrow> payments = escrowService.getEscrowsByFreelancer(freelancerId);

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
        alert.setTitle("Détails de mon paiement");
        alert.setHeaderText("Contrat #" + escrow.getContractId());

        VBox content = new VBox();
        content.setSpacing(10);
        content.setPadding(new Insets(10));

        String statusInfo = switch (escrow.getStatus()) {
            case "PENDING", "COMPLETED" ->
                "💰 Votre montant est actuellement bloqué chez l'admin. Il sera libéré après validation.";
            case "RELEASED" ->
                "✅ Votre montant a été libéré. Vous pouvez le retirer via votre portefeuille.";
            case "REFUNDED" ->
                "🔄 Ce paiement a été remboursé au client.";
            default -> "";
        };

        content.getChildren().addAll(
            new Label("Montant: " + String.format("%.2f TND", escrow.getAmount().doubleValue())),
            new Label("Statut: " + translateStatus(escrow.getStatus())),
            new Separator(),
            new Label(statusInfo),
            new Separator(),
            new Label("Date: " + (escrow.getDateCreation() != null ?
                escrow.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A"))
        );

        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    /**
     * Met à jour les statistiques
     */
    private void updateStatistics(List<PaymentEscrow> payments) {
        BigDecimal totalPending = payments.stream()
            .filter(p -> p.getStatus().equals("PENDING") || p.getStatus().equals("COMPLETED"))
            .map(PaymentEscrow::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAvailable = payments.stream()
            .filter(p -> p.getStatus().equals("RELEASED"))
            .map(PaymentEscrow::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPendingLabel != null) {
            totalPendingLabel.setText(String.format("%.2f TND", totalPending.doubleValue()));
        }
        if (totalAvailableLabel != null) {
            totalAvailableLabel.setText(String.format("%.2f TND", totalAvailable.doubleValue()));
        }
    }

    /**
     * Traduit le statut en français
     */
    private String translateStatus(String status) {
        return switch (status) {
            case "PENDING" -> "Bloqué";
            case "COMPLETED" -> "Livré";
            case "RELEASED" -> "Libéré";
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
            case "Livré" -> "COMPLETED";
            case "Libéré" -> "RELEASED";
            case "Remboursé" -> "REFUNDED";
            default -> status;
        };
    }

    /**
     * Retourne le style CSS pour un statut
     */
    private String getStatusStyle(String status) {
        return switch (status) {
            case "PENDING", "COMPLETED" -> "-fx-text-fill: #FF9800; -fx-font-weight: bold;";
            case "RELEASED" -> "-fx-text-fill: #4CAF50; -fx-font-weight: bold;";
            case "REFUNDED" -> "-fx-text-fill: #F44336; -fx-font-weight: bold;";
            default -> "";
        };
    }
}
