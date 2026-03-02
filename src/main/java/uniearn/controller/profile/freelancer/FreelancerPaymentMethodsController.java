package uniearn.controller.profile.freelancer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import uniearn.model.entities.BankAccount;
import uniearn.services.BankAccountService;
import uniearn.model.enums.UserRole;

import java.util.List;

/**
 * Contrôleur pour la gestion des moyens de paiement (comptes bancaires) du freelancer
 */
public class FreelancerPaymentMethodsController {
    @FXML private VBox bankAccountsContainer;
    @FXML private TextField accountHolderNameField;
    @FXML private TextField bankNameField;
    @FXML private TextField ibanField;
    @FXML private Button addAccountButton;
    @FXML private Button cancelButton;
    @FXML private Label messageLabel;

    private BankAccountService bankAccountService;
    private int currentUserID;

    @FXML
    public void initialize() {
        bankAccountService = new BankAccountService();
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        addAccountButton.setOnAction(e -> handleAddAccount());
        cancelButton.setOnAction(e -> clearForm());
    }

    /**
     * Charger les comptes bancaires de l'utilisateur
     */
    public void setUserID(int userID) {
        this.currentUserID = userID;
        loadBankAccounts();
    }

    private void loadBankAccounts() {
        bankAccountsContainer.getChildren().clear();

        List<BankAccount> accounts = bankAccountService.getBankAccountsByUser(currentUserID);

        if (accounts.isEmpty()) {
            Label noAccountsLabel = new Label("Aucun compte bancaire enregistré");
            noAccountsLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #666;");
            bankAccountsContainer.getChildren().add(noAccountsLabel);
        } else {
            for (BankAccount account : accounts) {
                VBox accountCard = createAccountCard(account);
                bankAccountsContainer.getChildren().add(accountCard);
            }
        }
    }

    private VBox createAccountCard(BankAccount account) {
        VBox card = new VBox(8);
        card.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #f9f9f9;");

        // Titre du compte
        HBox titleBox = new HBox(10);
        Label nameLabel = new Label(account.getAccountHolderName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        if (account.isDefault()) {
            Label defaultLabel = new Label("Par défaut");
            defaultLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 2 8; -fx-border-radius: 3;");
            titleBox.getChildren().addAll(nameLabel, defaultLabel);
        } else {
            titleBox.getChildren().add(nameLabel);
        }

        // Informations du compte
        Label bankLabel = new Label("Banque: " + account.getBankName());
        String ibanMasked = maskIban(account.getIban());
        Label ibanLabel = new Label("IBAN: " + ibanMasked);
        ibanLabel.setStyle("-fx-font-family: monospace;");

        // Boutons d'action
        HBox actionBox = new HBox(8);
        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-padding: 5 15;");
        editButton.setOnAction(e -> handleEditAccount(account));

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-padding: 5 15; -fx-text-fill: #d32f2f;");
        deleteButton.setOnAction(e -> handleDeleteAccount(account));

        Button setDefaultButton = new Button("Définir par défaut");
        setDefaultButton.setStyle("-fx-padding: 5 15;");
        if (account.isDefault()) {
            setDefaultButton.setDisable(true);
        }
        setDefaultButton.setOnAction(e -> handleSetDefault(account));

        actionBox.getChildren().addAll(editButton, setDefaultButton, deleteButton);

        card.getChildren().addAll(titleBox, bankLabel, ibanLabel, actionBox);
        return card;
    }

    private void handleAddAccount() {
        String accountHolderName = accountHolderNameField.getText().trim();
        String bankName = bankNameField.getText().trim();
        String iban = ibanField.getText().trim();

        if (accountHolderName.isEmpty() || bankName.isEmpty() || iban.isEmpty()) {
            showMessage("Veuillez remplir tous les champs", "error");
            return;
        }

        if (!isValidIBAN(iban)) {
            showMessage("IBAN invalide. Veuillez vérifier le format", "error");
            return;
        }

        BankAccount account = new BankAccount(currentUserID, accountHolderName, iban, "", bankName);

        if (bankAccountService.addBankAccount(account)) {
            showMessage("Compte bancaire ajouté avec succès", "success");
            clearForm();
            loadBankAccounts();
        } else {
            showMessage("Erreur lors de l'ajout du compte", "error");
        }
    }

    private void handleEditAccount(BankAccount account) {
        accountHolderNameField.setText(account.getAccountHolderName());
        bankNameField.setText(account.getBankName());
        ibanField.setText(account.getIban());

        addAccountButton.setText("Mettre à jour");
        addAccountButton.setOnAction(e -> {
            account.setAccountHolderName(accountHolderNameField.getText());
            account.setBankName(bankNameField.getText());
            account.setIban(ibanField.getText());

            if (bankAccountService.updateBankAccount(account)) {
                showMessage("Compte mis à jour avec succès", "success");
                clearForm();
                addAccountButton.setText("Ajouter un compte");
                loadBankAccounts();
            }
        });
    }

    private void handleDeleteAccount(BankAccount account) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer ce compte bancaire?");
        alert.setContentText("Cette action ne peut pas être annulée.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (bankAccountService.deleteBankAccount(account.getId())) {
                showMessage("Compte supprimé avec succès", "success");
                loadBankAccounts();
            } else {
                showMessage("Erreur lors de la suppression", "error");
            }
        }
    }

    private void handleSetDefault(BankAccount account) {
        account.setDefault(true);
        if (bankAccountService.updateBankAccount(account)) {
            showMessage("Compte défini comme par défaut", "success");
            loadBankAccounts();
        } else {
            showMessage("Erreur lors de la mise à jour", "error");
        }
    }

    private void clearForm() {
        accountHolderNameField.clear();
        bankNameField.clear();
        ibanField.clear();
        messageLabel.setText("");
        addAccountButton.setText("Ajouter un compte");
    }

    private void showMessage(String message, String type) {
        messageLabel.setText(message);
        if ("success".equals(type)) {
            messageLabel.setStyle("-fx-text-fill: #4CAF50;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #d32f2f;");
        }
    }

    private boolean isValidIBAN(String iban) {
        // Validation simple IBAN: 15-34 caractères alphanumériques
        return iban.length() >= 15 && iban.length() <= 34 && iban.matches("[A-Z0-9]+");
    }

    private String maskIban(String iban) {
        if (iban == null || iban.length() < 8) {
            return iban;
        }
        String start = iban.substring(0, 4);
        String end = iban.substring(iban.length() - 4);
        return start + "****" + end;
    }
}

