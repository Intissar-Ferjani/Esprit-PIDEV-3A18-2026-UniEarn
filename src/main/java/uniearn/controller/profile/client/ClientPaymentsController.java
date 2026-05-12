package uniearn.controller.profile.client;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import uniearn.model.entities.contracts.Contrat;
import uniearn.model.entities.users.client.Client;
import uniearn.services.contracts.ContratService;
import uniearn.services.payment.EscrowPaymentService;
import uniearn.services.payment.StripePaymentService;
import uniearn.services.users.client.ClientService;
import uniearn.database.SessionManager;
import java.awt.Desktop;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller pour la page des paiements Stripe côté Client
 * Affiche les contrats à payer et les contrats déjà payés via Stripe
 *
 * ⚠️ NOUVELLE VERSION - Utilise Stripe pour les paiements
 */
@SuppressWarnings({"deprecation", "unused"})
public class ClientPaymentsController {

    @FXML private Label statToPayCount;
    @FXML private Label statPaidCount;
    @FXML private Label statAmountDue;
    @FXML private Label statAmountSecured;

    @FXML private TableView<Contrat> toPayTable;
    @FXML private TableColumn<Contrat, Integer> toPayColId;
    @FXML private TableColumn<Contrat, String> toPayColTitle;
    @FXML private TableColumn<Contrat, String> toPayColFreelancer;
    @FXML private TableColumn<Contrat, Double> toPayColAmount;
    @FXML private TableColumn<Contrat, String> toPayColSignedOn;
    @FXML private TableColumn<Contrat, Void> toPayColActions;

    @FXML private TableView<Contrat> paidTable;
    @FXML private TableColumn<Contrat, Integer> paidColId;
    @FXML private TableColumn<Contrat, String> paidColTitle;
    @FXML private TableColumn<Contrat, String> paidColFreelancer;
    @FXML private TableColumn<Contrat, Double> paidColAmount;
    @FXML private TableColumn<Contrat, String> paidColStatus;
    @FXML private TableColumn<Contrat, Void> paidColActions;

    private final ContratService contratService = new ContratService();
    private final ClientService clientService = new ClientService();
    private final EscrowPaymentService escrowService = new EscrowPaymentService();
    private final StripePaymentService stripeService = new StripePaymentService();

    private Client currentClient;
    private final ObservableList<Contrat> toPayList = FXCollections.observableArrayList();
    private final ObservableList<Contrat> paidList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initializeStripe();
        loadCurrentClient();
        resolveInjectedColumns();
        setupTables();
        loadPayments();
    }

    private void initializeStripe() {
        // Charger la clé Stripe depuis un fichier de configuration ou variable d'environnement
        String stripeKey = loadStripeKey();
        if (stripeKey != null && !stripeKey.isEmpty()) {
            StripePaymentService.initialize(stripeKey);
        } else {
            System.out.println("⚠ Stripe key not configured - using simulation mode");
        }
    }

    private String loadStripeKey() {
        // Liste des chemins possibles pour le fichier .env
        String[] paths = {".env", "../.env", "src/main/resources/.env", "./.env"};
        
        for (String path : paths) {
            try {
                java.io.File envFile = new java.io.File(path);
                if (envFile.exists()) {
                    System.out.println("DEBUG: Loading Stripe key from: " + envFile.getAbsolutePath());
                    java.util.Scanner scanner = new java.util.Scanner(envFile);
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine().trim();
                        if (line.startsWith("STRIPE_SECRET_KEY=")) {
                            String key = line.substring("STRIPE_SECRET_KEY=".length()).trim();
                            scanner.close();
                            return key;
                        }
                    }
                    scanner.close();
                }
            } catch (Exception e) {
                System.err.println("Error loading Stripe key from " + path + ": " + e.getMessage());
            }
        }
        return null;
    }

    private void loadCurrentClient() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            showError("Erreur", "Vous devez être connecté");
            return;
        }

        int userId = SessionManager.getInstance().getCurrentUserId();
        currentClient = clientService.getClientById(userId);

        if (currentClient == null) {
            showError("Erreur", "Client introuvable");
        }
    }

    private void setupTables() {
        if (toPayTable == null || paidTable == null) {
            System.err.println("ERROR: Payment tables are not injected from FXML");
            return;
        }

        // Table "À Payer"
        toPayColId.setCellValueFactory(data ->
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getIdContract()));
        toPayColTitle.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getType()));
        toPayColFreelancer.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getFreelancerID() > 0 ?
                "Freelancer #" + data.getValue().getFreelancerID() : "N/A"));
        toPayColAmount.setCellValueFactory(data ->
            new SimpleDoubleProperty(data.getValue().getAmount()).asObject());
        toPayColSignedOn.setCellValueFactory(data -> {
            if (data.getValue().getFreelancerSignatureDate() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return new SimpleStringProperty(
                    data.getValue().getFreelancerSignatureDate().toLocalDateTime().format(formatter));
            }
            return new SimpleStringProperty("—");
        });

        // Colonne Actions pour "À Payer"
        toPayColActions.setCellFactory(param -> new TableCell<>() {
            private final Button payButton = new Button("💳 Pay Now");
            private final Button viewButton = new Button("👁 View");

            {
                payButton.setStyle("-fx-background-color: #f59e0b; " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; " +
                        "-fx-padding: 6 16; -fx-border-radius: 8; -fx-background-radius: 8;");
                viewButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; " +
                        "-fx-font-weight: bold; -fx-cursor: hand; " +
                        "-fx-padding: 6 14; -fx-border-radius: 8; -fx-background-radius: 8;");

                payButton.setOnAction(event -> {
                    Contrat contract = getTableView().getItems().get(getIndex());
                    handlePayment(contract);
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
                    HBox buttons = new HBox(8, payButton, viewButton);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        // Table "Payé"
        paidColId.setCellValueFactory(data ->
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getIdContract()));
        paidColTitle.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getType()));
        paidColFreelancer.setCellValueFactory(data ->
            new SimpleStringProperty("Freelancer #" + data.getValue().getFreelancerID()));
        paidColAmount.setCellValueFactory(data ->
            new SimpleDoubleProperty(data.getValue().getAmount()).asObject());
        paidColStatus.setCellValueFactory(data ->
            new SimpleStringProperty(getStatusBadge(data.getValue().getStatus())));

        // Colonne Actions pour "Payé"
        paidColActions.setCellFactory(param -> new TableCell<>() {
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

        toPayTable.setItems(toPayList);
        paidTable.setItems(paidList);
    }

    private void loadPayments() {
        if (currentClient == null) {
            System.err.println("ERROR: Cannot load payments because currentClient is null");
            return;
        }

        System.out.println("DEBUG: Loading payments for Client ID: " + currentClient.getIdClient());
        List<Contrat> allContracts = contratService.getContratsByClient(currentClient.getIdClient());
        System.out.println("DEBUG: Total contracts in DB: " + allContracts.size());

        // Séparer en "À Payer" (status = 3 = signed) et "Payé" (status >= 4)
        List<Contrat> toPay = new ArrayList<>();
        List<Contrat> paid = new ArrayList<>();

        for (Contrat c : allContracts) {
            int status = c.getStatus();

            // LOGIQUE DE TRI
            if (status == 3) {
                toPay.add(c);
            } else if (status >= 4) {
                paid.add(c);
            }
        }

        System.out.println("DEBUG: toPay size: " + toPay.size() + ", paid size: " + paid.size());

        toPayList.setAll(toPay);
        paidList.setAll(paid);

        updateStats(toPay, paid);
    }

    private void updateStats(List<Contrat> toPay, List<Contrat> paid) {
        if (statToPayCount != null) statToPayCount.setText(String.valueOf(toPay.size()));
        if (statPaidCount != null) statPaidCount.setText(String.valueOf(paid.size()));

        double totalDue = toPay.stream().mapToDouble(Contrat::getAmount).sum();
        double totalSecured = paid.stream().mapToDouble(Contrat::getAmount).sum();

        if (statAmountDue != null) statAmountDue.setText(String.format("$%.2f", totalDue));
        if (statAmountSecured != null) statAmountSecured.setText(String.format("$%.2f", totalSecured));
    }

    private void handlePayment(Contrat contract) {
        if (contract.getStatus() != 3) { // 3 = signed
            showError("Erreur", "Le contrat doit être signé par les deux parties avant le paiement.");
            return;
        }

        // Confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer le paiement");
        confirmAlert.setHeaderText("Payer le contrat: " + contract.getType());
        confirmAlert.setContentText(String.format("Montant: $%.2f\n\nVoulez-vous procéder au paiement?",
                contract.getAmount()));

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                processPayment(contract);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void resolveInjectedColumns() {
        if (toPayTable != null) {
            if (toPayColId == null && !toPayTable.getColumns().isEmpty()) toPayColId = (TableColumn<Contrat, Integer>) toPayTable.getColumns().get(0);
            if (toPayColTitle == null && toPayTable.getColumns().size() > 1) toPayColTitle = (TableColumn<Contrat, String>) toPayTable.getColumns().get(1);
            if (toPayColFreelancer == null && toPayTable.getColumns().size() > 2) toPayColFreelancer = (TableColumn<Contrat, String>) toPayTable.getColumns().get(2);
            if (toPayColAmount == null && toPayTable.getColumns().size() > 3) toPayColAmount = (TableColumn<Contrat, Double>) toPayTable.getColumns().get(3);
            if (toPayColSignedOn == null && toPayTable.getColumns().size() > 4) toPayColSignedOn = (TableColumn<Contrat, String>) toPayTable.getColumns().get(4);
            if (toPayColActions == null && toPayTable.getColumns().size() > 5) toPayColActions = (TableColumn<Contrat, Void>) toPayTable.getColumns().get(5);
        }

        if (paidTable != null) {
            if (paidColId == null && !paidTable.getColumns().isEmpty()) paidColId = (TableColumn<Contrat, Integer>) paidTable.getColumns().get(0);
            if (paidColTitle == null && paidTable.getColumns().size() > 1) paidColTitle = (TableColumn<Contrat, String>) paidTable.getColumns().get(1);
            if (paidColFreelancer == null && paidTable.getColumns().size() > 2) paidColFreelancer = (TableColumn<Contrat, String>) paidTable.getColumns().get(2);
            if (paidColAmount == null && paidTable.getColumns().size() > 3) paidColAmount = (TableColumn<Contrat, Double>) paidTable.getColumns().get(3);
            if (paidColStatus == null && paidTable.getColumns().size() > 4) paidColStatus = (TableColumn<Contrat, String>) paidTable.getColumns().get(4);
            if (paidColActions == null && paidTable.getColumns().size() > 5) paidColActions = (TableColumn<Contrat, Void>) paidTable.getColumns().get(5);
        }
    }

    private void processPayment(Contrat contract) {
        try {
            if (StripePaymentService.isInitialized()) {
                // Mode Stripe réel
                String successUrl = "uniearn://payment/success/" + contract.getIdContract();
                String cancelUrl = "uniearn://payment/cancel/" + contract.getIdContract();

                StripePaymentService.CheckoutSessionInfo checkoutSession = stripeService.createCheckoutSessionInfo(
                        contract.getIdContract(),
                        contract.getType(),
                        "Freelancer #" + contract.getFreelancerID(),
                        contract.getAmount(),
                        successUrl,
                        cancelUrl
                );

                // Ouvrir le navigateur avec l'URL Stripe
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(checkoutSession.getUrl()));

                    showInfo("Redirection Stripe", """
                            Vous allez être redirigé vers Stripe pour effectuer le paiement de manière sécurisée.

                            Une fois le paiement effectué, revenez à cette fenêtre et cliquez sur 'Rafraîchir'.
                            """);

                    monitorStripePayment(contract.getIdContract(), checkoutSession.getSessionId());
                }

            } else {
                // Mode simulation
                boolean success = stripeService.simulatePayment();
                if (success) {
                    // Mettre à jour le statut du contrat
                    contract.setStatus(4); // 4 = funded
                    contratService.updateContrat(contract);

                    // Créer l'entrée en Séquestre pour l'Admin
                    escrowService.createEscrow(
                        contract.getIdContract(),
                        contract.getClientID(),
                        contract.getFreelancerID(),
                        java.math.BigDecimal.valueOf(contract.getAmount())
                    );

                    showSuccess("Paiement simulé",
                            "💳 Paiement réussi ! L'argent est désormais sécurisé en séquestre par UniEarn.");

                    // Recharger les données
                    loadPayments();
                }
            }

        } catch (Exception e) {
            showError("Erreur de paiement", "Erreur lors du traitement du paiement: " + e.getMessage());
            System.err.println("Erreur Stripe: " + e.getMessage());
        }
    }

    private void monitorStripePayment(int contractId, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        Thread watcher = new Thread(() -> {
            try {
                for (int i = 0; i < 120; i++) { // ~10 minutes
                    if (stripeService.isSessionCompleted(sessionId)) {
                        Platform.runLater(() -> handlePaymentSuccess(contractId));
                        return;
                    }
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("Erreur de surveillance Stripe: " + e.getMessage());
            }
        }, "stripe-payment-watcher-" + contractId);

        watcher.setDaemon(true);
        watcher.start();
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
            case 4 -> "🔒 Funded";
            case 5 -> "✅ Released";
            case 6 -> "✅ Completed";
            default -> "Unknown";
        };
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

    private void handleManualPayment(Contrat contract) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Validation Manuelle");
        confirm.setHeaderText("Confirmer le paiement du contrat #" + contract.getIdContract());
        confirm.setContentText("""
                Avez-vous bien payé ce contrat sur Stripe ?

                Cette action va forcer le statut à 'Payé' et sécuriser l'argent en séquestre.
                """);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // 1. Mettre à jour le statut du contrat
                    contract.setStatus(4); // funded
                    contratService.updateContractStatus(contract.getIdContract(), 4);

                    // 2. Créer l'entrée en Séquestre
                    escrowService.createEscrow(
                            contract.getIdContract(),
                            contract.getClientID(),
                            contract.getFreelancerID(),
                            java.math.BigDecimal.valueOf(contract.getAmount())
                    );

                    showSuccess("Succès", "Le contrat a été marqué comme payé avec succès !");
                    loadPayments();
                } catch (Exception e) {
                    showError("Erreur", "Impossible de valider le paiement: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Méthode appelée par un webhook ou callback après succès du paiement Stripe
     */
    public void handlePaymentSuccess(int contractId) {
        Contrat contract = contratService.getContratById(contractId);
        if (contract != null) {
            boolean changed = false;

            if (contract.getStatus() == 3) {
                contract.setStatus(4); // funded
                contratService.updateContrat(contract);
                changed = true;
            }

            if (escrowService.getEscrowByContractId(contractId) == null) {
                escrowService.createEscrow(
                        contract.getIdContract(),
                        contract.getClientID(),
                        contract.getFreelancerID(),
                        java.math.BigDecimal.valueOf(contract.getAmount())
                );
                changed = true;
            }

            if (changed) {
                loadPayments();
                showSuccess("Paiement confirmé",
                        "✅ Le paiement a été confirmé par Stripe. Le contrat a été déplacé vers le tableau des paiements.");
            }
        }
    }
}


