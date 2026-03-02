package uniearn.controller.profile.admin;

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

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contrôleur pour la gestion des paiements en escrow par l'admin
 */
public class AdminPaymentsController {

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
    private TableColumn<PaymentEscrow, String> colClient;

    @FXML
    private TableColumn<PaymentEscrow, String> colFreelancer;

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

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadPayments();
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

            // Colonne Client ID
            colClient.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                    String.valueOf(cellData.getValue().getClientId())
                )
            );

            // Colonne Freelancer ID
            colFreelancer.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                    String.valueOf(cellData.getValue().getFreelancerId())
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
     * Charge tous les paiements en attente de validation
     */
    public void loadPayments() {
        new Thread(() -> {
            try {
                List<PaymentEscrow> payments = escrowService.getPendingEscrows();

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
                                    String.valueOf(p.getClientId()).contains(searchText) ||
                                    String.valueOf(p.getFreelancerId()).contains(searchText) ||
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

        // Bouton de validation (marquer comme livré)
        if (escrow.getStatus().equals("PENDING")) {
            Button btnMarkCompleted = new Button("Marquer livré");
            btnMarkCompleted.setStyle("-fx-padding: 5px 15px; -fx-font-size: 12px; -fx-text-fill: #FF9800;");
            btnMarkCompleted.setOnAction(e -> {
                if (escrowService.markAsCompleted(escrow.getId())) {
                    showAlert("✅ Succès", "Escrow marqué comme livré");
                    loadPayments();
                }
            });
            hbox.getChildren().add(btnMarkCompleted);
        }

        // Bouton de libération (pour les escrows complétés)
        if (escrow.getStatus().equals("COMPLETED")) {
            Button btnRelease = new Button("Libérer le paiement");
            btnRelease.setStyle("-fx-padding: 5px 15px; -fx-font-size: 12px; -fx-text-fill: #4CAF50;");
            btnRelease.setOnAction(e -> showReleaseDialog(escrow));
            hbox.getChildren().add(btnRelease);
        }

        // Bouton de remboursement
        if (escrow.getStatus().equals("PENDING") || escrow.getStatus().equals("COMPLETED")) {
            Button btnRefund = new Button("Rembourser");
            btnRefund.setStyle("-fx-padding: 5px 15px; -fx-font-size: 12px; -fx-text-fill: #F44336;");
            btnRefund.setOnAction(e -> showRefundDialog(escrow));
            hbox.getChildren().add(btnRefund);
        }

        hbox.getChildren().add(btnDetails);
        return hbox;
    }

    /**
     * Affiche les détails d'un paiement
     */
    private void showPaymentDetails(PaymentEscrow escrow) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du paiement en escrow");
        alert.setHeaderText("Escrow #" + escrow.getId());

        VBox content = new VBox();
        content.setSpacing(10);
        content.setPadding(new Insets(10));

        content.getChildren().addAll(
            new Label("📋 Contrat ID: " + escrow.getContractId()),
            new Label("👤 Client ID: " + escrow.getClientId()),
            new Label("👨‍💼 Freelancer ID: " + escrow.getFreelancerId()),
            new Label("💰 Montant: " + String.format("%.2f TND", escrow.getAmount().doubleValue())),
            new Label("📊 Statut: " + translateStatus(escrow.getStatus())),
            new Separator(),
            new Label("📅 Création: " + (escrow.getDateCreation() != null ?
                escrow.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A")),
            new Label("✅ Livraison: " + (escrow.getDateCompletion() != null ?
                escrow.getDateCompletion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Pas encore")),
            new Label("🔓 Libération: " + (escrow.getDateLiberation() != null ?
                escrow.getDateLiberation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Pas encore")),
            new Separator(),
            new Label("📝 Notes: " + (escrow.getNotes() != null ? escrow.getNotes() : "Aucune note"))
        );

        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    /**
     * Affiche le dialog de libération de paiement
     */
    private void showReleaseDialog(PaymentEscrow escrow) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Libérer le paiement");
        dialog.setHeaderText("Libérer le montant de " + String.format("%.2f TND", escrow.getAmount().doubleValue()) +
                            " au freelancer #" + escrow.getFreelancerId());

        VBox content = new VBox();
        content.setSpacing(10);
        content.setPadding(new Insets(10));

        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(4);
        notesArea.setWrapText(true);
        notesArea.setPromptText("Notes optionnelles sur la validation (ex: travail de bonne qualité)");

        content.getChildren().addAll(
            new Label("Êtes-vous sûr de vouloir libérer ce paiement?"),
            new Separator(),
            new Label("Notes:"),
            notesArea
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String notes = notesArea.getText();
                if (escrowService.releasePayment(escrow.getId(), notes)) {
                    showAlert("✅ Succès", "Montant libéré au freelancer");
                    loadPayments();
                } else {
                    showAlert("❌ Erreur", "Impossible de libérer le paiement");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Affiche le dialog de remboursement
     */
    private void showRefundDialog(PaymentEscrow escrow) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Rembourser le client");
        dialog.setHeaderText("Rembourser " + String.format("%.2f TND", escrow.getAmount().doubleValue()) +
                            " au client #" + escrow.getClientId());

        VBox content = new VBox();
        content.setSpacing(10);
        content.setPadding(new Insets(10));

        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(4);
        notesArea.setWrapText(true);
        notesArea.setPromptText("Raison du remboursement");

        content.getChildren().addAll(
            new Label("Êtes-vous sûr de vouloir rembourser ce montant?"),
            new Separator(),
            new Label("Raison du remboursement:"),
            notesArea
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String reason = notesArea.getText();
                if (escrowService.refundPayment(escrow.getId(), reason)) {
                    showAlert("✅ Succès", "Paiement remboursé au client");
                    loadPayments();
                } else {
                    showAlert("❌ Erreur", "Impossible de rembourser le paiement");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Met à jour les statistiques
     */
    private void updateStatistics(List<PaymentEscrow> payments) {
        BigDecimal totalBlocked = escrowService.getTotalBlockedAmount();

        BigDecimal totalReleased = payments.stream()
            .filter(p -> p.getStatus().equals("RELEASED"))
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
     * Affiche une alerte
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
            case "PENDING" -> "-fx-text-fill: #FF9800; -fx-font-weight: bold;";
            case "COMPLETED" -> "-fx-text-fill: #2196F3; -fx-font-weight: bold;";
            case "RELEASED" -> "-fx-text-fill: #4CAF50; -fx-font-weight: bold;";
            case "REFUNDED" -> "-fx-text-fill: #F44336; -fx-font-weight: bold;";
            default -> "";
        };
    }
}

