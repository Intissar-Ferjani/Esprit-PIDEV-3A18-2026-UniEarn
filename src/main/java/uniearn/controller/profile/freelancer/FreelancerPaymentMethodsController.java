package uniearn.controller.profile.freelancer;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import uniearn.database.SessionManager;
import uniearn.model.entities.BankAccount;
import uniearn.services.BankAccountService;

import java.util.List;

/**
 * Contrôleur pour la gestion des moyens de paiement (comptes bancaires) côté freelancer.
 */
public class FreelancerPaymentMethodsController {

    @FXML private VBox accountsContainer;
    @FXML private TextField holderNameField;
    @FXML private TextField ibanField;
    @FXML private TextField bicField;
    @FXML private TextField bankNameField;
    @FXML private CheckBox defaultCheckBox;
    @FXML private Label statusLabel;
    @FXML private VBox formPane;

    private final BankAccountService bankAccountService = new BankAccountService();
    private int userId = -1;
    private BankAccount selectedAccount = null;

    @FXML
    public void initialize() {
        if (SessionManager.getInstance().isLoggedIn()) {
            userId = SessionManager.getInstance().getCurrentUserId();
        }
        loadAccounts();
        clearForm();
    }

    private void loadAccounts() {
        if (accountsContainer == null) return;
        accountsContainer.getChildren().clear();

        if (userId <= 0) {
            showStatus("Aucun utilisateur connecté", true);
            return;
        }

        List<BankAccount> accounts = bankAccountService.getBankAccountsByUser(userId);

        if (accounts.isEmpty()) {
            Label emptyLabel = new Label("Aucun compte bancaire enregistré.");
            emptyLabel.setStyle("-fx-text-fill: #5e6d55; -fx-font-size: 13px;");
            emptyLabel.setPadding(new Insets(16));
            accountsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (BankAccount account : accounts) {
            accountsContainer.getChildren().add(createAccountCard(account));
        }
    }

    private HBox createAccountCard(BankAccount account) {
        HBox card = new HBox(14);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle("-fx-background-color: white; -fx-border-color: #e4ebe4; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");

        VBox info = new VBox(4);
        Label nameLabel = new Label(account.getBankName() +
                (account.isDefault() ? "  ★ Par défaut" : ""));
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #001e00; -fx-font-size: 14px;");
        Label holderLabel = new Label(account.getAccountHolderName());
        holderLabel.setStyle("-fx-text-fill: #5e6d55; -fx-font-size: 12px;");
        Label ibanLabel = new Label(account.getMaskedIban());
        ibanLabel.setStyle("-fx-text-fill: #5e6d55; -fx-font-size: 12px;");
        info.getChildren().addAll(nameLabel, holderLabel, ibanLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("✏ Modifier");
        editBtn.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-border-radius: 6; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> editAccount(account));

        Button deleteBtn = new Button("🗑 Supprimer");
        deleteBtn.setStyle("-fx-background-color: #fce4ec; -fx-text-fill: #d93025; -fx-border-radius: 6; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> deleteAccount(account));

        card.getChildren().addAll(info, spacer, editBtn, deleteBtn);
        return card;
    }

    private void editAccount(BankAccount account) {
        this.selectedAccount = account;
        if (holderNameField != null) holderNameField.setText(account.getAccountHolderName());
        if (ibanField != null) ibanField.setText(account.getIban());
        if (bicField != null) bicField.setText(account.getBic());
        if (bankNameField != null) bankNameField.setText(account.getBankName());
        if (defaultCheckBox != null) defaultCheckBox.setSelected(account.isDefault());
        showStatus("", false);
    }

    private void deleteAccount(BankAccount account) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le compte");
        confirm.setHeaderText("Supprimer ce compte bancaire ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (bankAccountService.deleteBankAccount(account.getId())) {
                    showStatus("✓ Compte supprimé avec succès", false);
                    loadAccounts();
                } else {
                    showStatus("❌ Erreur lors de la suppression", true);
                }
            }
        });
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) return;

        BankAccount account = selectedAccount != null ? selectedAccount : new BankAccount();
        account.setUserId(userId);
        account.setAccountHolderName(holderNameField.getText().trim());
        account.setIban(ibanField.getText().trim().toUpperCase().replaceAll("\\s+", ""));
        account.setBic(bicField.getText().trim().toUpperCase());
        account.setBankName(bankNameField.getText().trim());
        account.setDefault(defaultCheckBox != null && defaultCheckBox.isSelected());

        boolean success;
        if (selectedAccount != null) {
            success = bankAccountService.updateBankAccount(account);
        } else {
            success = bankAccountService.addBankAccount(account);
        }

        if (success) {
            showStatus("✓ Compte bancaire enregistré avec succès", false);
            clearForm();
            loadAccounts();
        } else {
            showStatus("❌ Erreur lors de l'enregistrement du compte", true);
        }
    }

    @FXML
    private void handleCancel() {
        clearForm();
        showStatus("", false);
    }

    private boolean validateForm() {
        if (holderNameField == null || holderNameField.getText().trim().isEmpty()) {
            showStatus("⚠ Le nom du titulaire est obligatoire", true);
            return false;
        }
        String iban = ibanField != null ? ibanField.getText().trim().replaceAll("\\s+", "") : "";
        if (iban.isEmpty() || iban.length() < 15 || iban.length() > 34 || !iban.matches("[A-Z0-9]+")) {
            showStatus("⚠ IBAN invalide (15-34 caractères alphanumériques, ex: TN59...)", true);
            return false;
        }
        if (bankNameField == null || bankNameField.getText().trim().isEmpty()) {
            showStatus("⚠ Le nom de la banque est obligatoire", true);
            return false;
        }
        return true;
    }

    private void clearForm() {
        selectedAccount = null;
        if (holderNameField != null) holderNameField.clear();
        if (ibanField != null) ibanField.clear();
        if (bicField != null) bicField.clear();
        if (bankNameField != null) bankNameField.clear();
        if (defaultCheckBox != null) defaultCheckBox.setSelected(false);
    }

    private void showStatus(String message, boolean isError) {
        if (statusLabel == null) return;
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + (isError ? "#d93025" : "#14a800") + ";");
        statusLabel.setVisible(!message.isEmpty());
    }
}
