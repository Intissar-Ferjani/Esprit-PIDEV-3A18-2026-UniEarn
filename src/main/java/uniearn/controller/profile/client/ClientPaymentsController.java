package uniearn.controller.profile.client;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import uniearn.database.SessionManager;
import uniearn.model.entities.EscrowPayment;
import uniearn.services.EscrowPaymentService;
import uniearn.services.users.client.ClientService;
import uniearn.model.entities.users.client.Client;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contrôleur pour l'affichage des paiements en escrow côté client.
 */
public class ClientPaymentsController {

    @FXML private VBox paymentsContainer;
    @FXML private Label totalAmountLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label completedCountLabel;
    @FXML private Label releasedCountLabel;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final ClientService clientService = new ClientService();
    private int clientId = -1;

    @FXML
    public void initialize() {
        resolveClientId();
        if (clientId > 0) {
            loadPayments();
        } else {
            showEmptyState("Aucun utilisateur connecté");
        }
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
            } else {
                clientId = userId;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du client: " + e.getMessage());
            clientId = userId;
        }
    }

    private void loadPayments() {
        try {
            List<EscrowPayment> payments = escrowService.getPaymentsByClient(clientId);

            if (paymentsContainer != null) {
                paymentsContainer.getChildren().clear();
            }

            if (payments.isEmpty()) {
                showEmptyState("Aucun paiement trouvé");
                updateStats(List.of());
                return;
            }

            updateStats(payments);

            for (EscrowPayment payment : payments) {
                if (paymentsContainer != null) {
                    paymentsContainer.getChildren().add(createPaymentCard(payment));
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des paiements: " + e.getMessage());
            showEmptyState("Erreur lors du chargement des paiements");
        }
    }

    private HBox createPaymentCard(EscrowPayment payment) {
        HBox card = new HBox(16);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setStyle("-fx-background-color: white; -fx-border-color: #e4ebe4; -fx-border-width: 0 0 1 0;");

        // Montant
        VBox amountBox = new VBox(4);
        Label amountLabel = new Label(String.format("%.2f DT", payment.getAmount()));
        amountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #001e00;");
        Label contractLabel = new Label("Contrat #" + payment.getContractId());
        contractLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #5e6d55;");
        amountBox.getChildren().addAll(amountLabel, contractLabel);

        // Statut
        Label statusBadge = new Label(payment.getStatusLabel());
        statusBadge.setPadding(new Insets(4, 10, 4, 10));
        statusBadge.setStyle("-fx-background-color: " + getStatusColor(payment.getStatus()) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: bold;");

        // Date
        Label dateLabel = new Label(payment.getCreatedAt() != null
                ? payment.getCreatedAt().toLocalDateTime().format(DATE_FMT)
                : "-");
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #5e6d55;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(amountBox, spacer, dateLabel, statusBadge);
        return card;
    }

    private void updateStats(List<EscrowPayment> payments) {
        double total = payments.stream().mapToDouble(EscrowPayment::getAmount).sum();
        long pending = payments.stream().filter(p -> "PENDING".equals(p.getStatus())).count();
        long completed = payments.stream().filter(p -> "COMPLETED".equals(p.getStatus())).count();
        long released = payments.stream().filter(p -> "RELEASED".equals(p.getStatus())).count();

        if (totalAmountLabel != null) totalAmountLabel.setText(String.format("%.2f DT", total));
        if (pendingCountLabel != null) pendingCountLabel.setText(String.valueOf(pending));
        if (completedCountLabel != null) completedCountLabel.setText(String.valueOf(completed));
        if (releasedCountLabel != null) releasedCountLabel.setText(String.valueOf(released));
    }

    private void showEmptyState(String message) {
        if (paymentsContainer == null) return;
        paymentsContainer.getChildren().clear();
        Label emptyLabel = new Label(message);
        emptyLabel.setStyle("-fx-text-fill: #5e6d55; -fx-font-size: 14px;");
        emptyLabel.setPadding(new Insets(20));
        paymentsContainer.getChildren().add(emptyLabel);
    }

    private String getStatusColor(String status) {
        if (status == null) return "#5e6d55";
        return switch (status) {
            case "PENDING"   -> "#ffa000";
            case "COMPLETED" -> "#1976d2";
            case "RELEASED"  -> "#14a800";
            case "REFUNDED"  -> "#d93025";
            default          -> "#5e6d55";
        };
    }

    @FXML
    private void handleRefresh() {
        if (clientId > 0) {
            loadPayments();
        }
    }
}
