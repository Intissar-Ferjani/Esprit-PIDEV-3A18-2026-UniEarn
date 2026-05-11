package uniearn.controller.profile.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import uniearn.model.entities.BankAccount;
import uniearn.services.payment.BankAccountService;
import uniearn.database.SessionManager;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur pour la gestion des moyens de paiement du client
 */
public class ClientPaymentMethodsController {

    @FXML
    private VBox methodsContainer;

    @FXML
    private ListView<BankAccount> accountsListView;

    @FXML
    private Button addAccountButton;

    private final BankAccountService bankAccountService = new BankAccountService();
    private final ObservableList<BankAccount> accountsList = FXCollections.observableArrayList();
    private int clientUserId;

    @FXML
    public void initialize() {
        if (SessionManager.getInstance().isLoggedIn()) {
            clientUserId = SessionManager.getInstance().getCurrentUserId();
        }

        setupListView();
        loadBankAccounts();
    }

    /**
     * Configure la ListView pour afficher les comptes
     */
    private void setupListView() {
        if (accountsListView != null) {
            accountsListView.setItems(accountsList);
            accountsListView.setCellFactory(param -> new BankAccountListCell());
        }

        if (addAccountButton != null) {
            addAccountButton.setOnAction(e -> showAddAccountDialog());
        }
    }

    /**
     * Charge les comptes bancaires de l'utilisateur
     */
    private void loadBankAccounts() {
        new Thread(() -> {
            try {
                List<BankAccount> accounts = bankAccountService.getBankAccountsByUserId(clientUserId);
                javafx.application.Platform.runLater(() -> {
                    accountsList.clear();
                    accountsList.addAll(accounts);
                    System.out.println("✅ " + accounts.size() + " comptes bancaires chargés");
                });
            } catch (Exception e) {
                System.err.println("❌ Erreur lors du chargement des comptes: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Affiche le dialog pour ajouter un nouveau compte
     */
    @FXML
    private void showAddAccountDialog() {
        Dialog<BankAccount> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un compte bancaire");
        dialog.setHeaderText("Veuillez entrer vos données bancaires");

        // Champs du formulaire
        TextField nameField = new TextField();
        nameField.setPromptText("Titulaire du compte");
        nameField.setPrefWidth(300);

        TextField ibanField = new TextField();
        ibanField.setPromptText("IBAN (ex: FR1420041010050500013M02606)");
        ibanField.setPrefWidth(300);

        TextField bicField = new TextField();
        bicField.setPromptText("BIC (ex: BNPAFRPP)");
        bicField.setPrefWidth(300);

        TextField bankField = new TextField();
        bankField.setPromptText("Nom de la banque");
        bankField.setPrefWidth(300);

        CheckBox defaultCheckBox = new CheckBox("Utiliser par défaut");

        VBox content = new VBox();
        content.setSpacing(12);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(
            new Label("Titulaire du compte:"),
            nameField,
            new Label("IBAN:"),
            ibanField,
            new Label("BIC:"),
            bicField,
            new Label("Banque:"),
            bankField,
            defaultCheckBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                // Validation
                if (nameField.getText().trim().isEmpty()) {
                    showAlert("Erreur", "Le nom du titulaire est requis");
                    return null;
                }
                if (ibanField.getText().trim().isEmpty()) {
                    showAlert("Erreur", "L'IBAN est requis");
                    return null;
                }
                if (!BankAccountService.isValidIBAN(ibanField.getText().trim())) {
                    showAlert("Erreur", "Format IBAN invalide");
                    return null;
                }
                if (bicField.getText().trim().isEmpty()) {
                    showAlert("Erreur", "Le BIC est requis");
                    return null;
                }

                BankAccount account = new BankAccount(
                    clientUserId,
                    nameField.getText().trim(),
                    ibanField.getText().trim().toUpperCase(),
                    bicField.getText().trim().toUpperCase(),
                    bankField.getText().trim().isEmpty() ? "Banque personnelle" : bankField.getText().trim()
                );
                account.setDefault(defaultCheckBox.isSelected());
                return account;
            }
            return null;
        });

        Optional<BankAccount> result = dialog.showAndWait();
        result.ifPresent(account -> {
            BankAccount saved = bankAccountService.addBankAccount(account);
            if (saved != null) {
                accountsList.add(saved);
                showSuccess("Succès", "Compte bancaire ajouté avec succès");
            } else {
                showAlert("Erreur", "Impossible d'ajouter le compte bancaire");
            }
        });
    }

    /**
     * Custom ListCell pour afficher les comptes bancaires
     */
    private class BankAccountListCell extends ListCell<BankAccount> {
        @Override
        protected void updateItem(BankAccount item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                VBox container = new VBox();
                container.setSpacing(5);
                container.setPadding(new Insets(10));
                container.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 10;");

                HBox header = new HBox();
                header.setSpacing(10);
                header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label nameLabel = new Label(item.getAccountHolderName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");

                Label bankLabel = new Label(item.getBankName());
                bankLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10;");

                if (item.isDefault()) {
                    Label defaultLabel = new Label("Par défaut");
                    defaultLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 9;");
                    header.getChildren().addAll(nameLabel, bankLabel, defaultLabel);
                } else {
                    header.getChildren().addAll(nameLabel, bankLabel);
                }

                Label ibanLabel = new Label("IBAN: " + maskIBAN(item.getIban()));
                ibanLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #999;");

                HBox actions = new HBox();
                actions.setSpacing(5);
                actions.setStyle("-fx-padding: 5;");

                Button editBtn = new Button("Modifier");
                editBtn.setStyle("-fx-padding: 5px 10px; -fx-font-size: 10;");
                editBtn.setOnAction(e -> showEditAccountDialog(item));

                Button deleteBtn = new Button("Supprimer");
                deleteBtn.setStyle("-fx-padding: 5px 10px; -fx-font-size: 10; -fx-text-fill: #F44336;");
                deleteBtn.setOnAction(e -> deleteAccount(item));

                actions.getChildren().addAll(editBtn, deleteBtn);

                container.getChildren().addAll(header, ibanLabel, actions);
                setGraphic(container);
            }
        }
    }

    /**
     * Affiche le dialog pour modifier un compte
     */
    private void showEditAccountDialog(BankAccount account) {
        Dialog<BankAccount> dialog = new Dialog<>();
        dialog.setTitle("Modifier le compte bancaire");
        dialog.setHeaderText("Modifiez vos données bancaires");

        TextField nameField = new TextField(account.getAccountHolderName());
        TextField ibanField = new TextField(account.getIban());
        TextField bicField = new TextField(account.getBic());
        TextField bankField = new TextField(account.getBankName());
        CheckBox defaultCheckBox = new CheckBox("Utiliser par défaut");
        defaultCheckBox.setSelected(account.isDefault());

        VBox content = new VBox();
        content.setSpacing(12);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(
            new Label("Titulaire du compte:"),
            nameField,
            new Label("IBAN:"),
            ibanField,
            new Label("BIC:"),
            bicField,
            new Label("Banque:"),
            bankField,
            defaultCheckBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if (nameField.getText().trim().isEmpty() || ibanField.getText().trim().isEmpty()) {
                    showAlert("Erreur", "Veuillez remplir tous les champs obligatoires");
                    return null;
                }
                account.setAccountHolderName(nameField.getText().trim());
                account.setIban(ibanField.getText().trim().toUpperCase());
                account.setBic(bicField.getText().trim().toUpperCase());
                account.setBankName(bankField.getText().trim());
                account.setDefault(defaultCheckBox.isSelected());
                return account;
            }
            return null;
        });

        Optional<BankAccount> result = dialog.showAndWait();
        result.ifPresent(updated -> {
            if (bankAccountService.updateBankAccount(updated)) {
                loadBankAccounts();
                showSuccess("Succès", "Compte bancaire modifié avec succès");
            } else {
                showAlert("Erreur", "Impossible de modifier le compte bancaire");
            }
        });
    }

    /**
     * Supprime un compte bancaire
     */
    private void deleteAccount(BankAccount account) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Êtes-vous sûr?");
        confirm.setContentText("Cette action ne peut pas être annulée.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (bankAccountService.deleteBankAccount(account.getId())) {
                accountsList.remove(account);
                showSuccess("Succès", "Compte bancaire supprimé");
            } else {
                showAlert("Erreur", "Impossible de supprimer le compte");
            }
        }
    }

    /**
     * Masque l'IBAN pour l'affichage (montre les 4 derniers caractères)
     */
    private String maskIBAN(String iban) {
        if (iban.length() > 4) {
            return "**** **** **** " + iban.substring(iban.length() - 4);
        }
        return "****";
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche un message de succès
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

