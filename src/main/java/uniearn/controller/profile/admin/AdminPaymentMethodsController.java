package uniearn.controller.profile.admin;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import uniearn.model.entities.BankAccount;
import uniearn.services.BankAccountService;

import java.util.List;

/**
 * Contrôleur admin pour la visualisation et gestion des comptes bancaires.
 */
public class AdminPaymentMethodsController {

    @FXML private VBox accountsContainer;
    @FXML private Label totalAccountsLabel;
    @FXML private Label statusLabel;

    private final BankAccountService bankAccountService = new BankAccountService();

    @FXML
    public void initialize() {
        loadAllAccounts();
    }

    private void loadAllAccounts() {
        if (accountsContainer == null) return;
        accountsContainer.getChildren().clear();

        List<BankAccount> accounts = bankAccountService.getAllBankAccounts();

        if (totalAccountsLabel != null) {
            totalAccountsLabel.setText(String.valueOf(accounts.size()));
        }

        if (accounts.isEmpty()) {
            Label emptyLabel = new Label("Aucun compte bancaire enregistré dans le système.");
            emptyLabel.setStyle("-fx-text-fill: #5e6d55; -fx-font-size: 13px;");
            emptyLabel.setPadding(new Insets(20));
            accountsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (BankAccount account : accounts) {
            accountsContainer.getChildren().add(createAccountRow(account));
        }
    }

    private HBox createAccountRow(BankAccount account) {
        HBox row = new HBox(16);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-background-color: white; -fx-border-color: #e4ebe4; " +
                "-fx-border-width: 0 0 1 0;");

        Label userIdLabel = new Label("User #" + account.getUserId());
        userIdLabel.setStyle("-fx-text-fill: #5e6d55; -fx-font-size: 12px;");
        userIdLabel.setPrefWidth(70);

        Label bankLabel = new Label(account.getBankName());
        bankLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #001e00;");
        bankLabel.setPrefWidth(140);

        Label holderLabel = new Label(account.getAccountHolderName());
        holderLabel.setStyle("-fx-text-fill: #5e6d55;");
        holderLabel.setPrefWidth(160);

        Label ibanLabel = new Label(account.getMaskedIban());
        ibanLabel.setStyle("-fx-text-fill: #5e6d55; -fx-font-family: monospace;");
        ibanLabel.setPrefWidth(140);

        Label defaultLabel = new Label(account.isDefault() ? "★ Défaut" : "");
        defaultLabel.setStyle("-fx-text-fill: #ffa000; -fx-font-weight: bold;");
        defaultLabel.setPrefWidth(70);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-background-color: #fce4ec; -fx-text-fill: #d93025; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> deleteAccount(account));

        row.getChildren().addAll(userIdLabel, bankLabel, holderLabel, ibanLabel, defaultLabel, spacer, deleteBtn);
        return row;
    }

    private void deleteAccount(BankAccount account) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le compte");
        confirm.setHeaderText("Supprimer ce compte bancaire ?");
        confirm.setContentText("Banque: " + account.getBankName() + "\nTitulaire: " + account.getAccountHolderName());
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (bankAccountService.deleteBankAccount(account.getId())) {
                    showStatus("✓ Compte supprimé avec succès", false);
                    loadAllAccounts();
                } else {
                    showStatus("❌ Erreur lors de la suppression", true);
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadAllAccounts();
        showStatus("", false);
    }

    private void showStatus(String message, boolean isError) {
        if (statusLabel == null) return;
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + (isError ? "#d93025" : "#14a800") + ";");
        statusLabel.setVisible(!message.isEmpty());
    }
}
