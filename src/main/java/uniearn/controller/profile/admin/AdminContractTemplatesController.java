package uniearn.controller.profile.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import uniearn.model.entities.contracts.ContractTemplate;
import uniearn.model.entities.users.admin.Admin;
import uniearn.services.contracts.ContractTemplateService;

import java.util.List;

/**
 * Contrôleur pour gérer les templates de contrats en tant qu'administrateur
 */
public class AdminContractTemplatesController {

    @FXML private VBox templatesContainer;
    @FXML private TextField searchField;
    @FXML private Label noTemplatesLabel;
    @FXML private ScrollPane scrollPane;
    @FXML private Button btnAddTemplate;

    private Admin currentAdmin;
    private final ContractTemplateService templateService = new ContractTemplateService();
    private ObservableList<ContractTemplate> templatesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupSearch();
        setupAddButton();
    }

    public void setAdminData(Admin admin) {
        this.currentAdmin = admin;
        loadTemplates();
    }

    private void setupAddButton() {
        if (btnAddTemplate != null) {
            btnAddTemplate.setOnAction(e -> showCreateTemplateDialog());
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (currentAdmin != null) {
                loadTemplates();
            }
        });
    }

    private void loadTemplates() {
        try {
            List<ContractTemplate> templates = templateService.getAllTemplates();

            String searchText = searchField.getText().toLowerCase();

            List<ContractTemplate> filtered = templates.stream()
                    .filter(t -> filterBySearch(t, searchText))
                    .toList();

            templatesList.setAll(filtered);
            displayTemplates(filtered);

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des templates: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean filterBySearch(ContractTemplate template, String searchText) {
        if (searchText.isEmpty()) return true;

        String name = template.getTemplateName() != null ? template.getTemplateName().toLowerCase() : "";
        String description = template.getDescription() != null ? template.getDescription().toLowerCase() : "";

        return name.contains(searchText) || description.contains(searchText);
    }

    private void displayTemplates(List<ContractTemplate> templates) {
        templatesContainer.getChildren().clear();

        if (templates.isEmpty()) {
            noTemplatesLabel.setText("Aucun template trouvé");
            noTemplatesLabel.setVisible(true);
            return;
        }

        noTemplatesLabel.setVisible(false);

        for (ContractTemplate template : templates) {
            VBox templateCard = createTemplateCard(template);
            templatesContainer.getChildren().add(templateCard);
        }
    }

    private VBox createTemplateCard(ContractTemplate template) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white; " +
                "-fx-border-color: #e2e8f0; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );
        card.setPrefWidth(Double.MAX_VALUE);

        // En-tête
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(template.getTemplateName() != null ? template.getTemplateName() : "Template");
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1a202c;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label idBadge = new Label("ID: #" + template.getIdTemplate());
        idBadge.setStyle(
                "-fx-background-color: #e2e8f020; " +
                "-fx-text-fill: #718096; " +
                "-fx-padding: 5 10; " +
                "-fx-background-radius: 12; " +
                "-fx-font-size: 11;"
        );

        header.getChildren().addAll(nameLabel, spacer, idBadge);

        // Détails
        VBox details = new VBox(8);
        details.setStyle("-fx-text-fill: #4a5568;");

        if (template.getDescription() != null && !template.getDescription().isEmpty()) {
            Label descLabel = new Label(template.getDescription());
            descLabel.setWrapText(true);
            descLabel.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 13;");
            details.getChildren().add(descLabel);
        }

        // Informations
        HBox infoBox = new HBox(20);
        infoBox.setStyle("-fx-font-size: 12;");

        Label dateLabel = new Label("Créé: " + (template.getCreatedDate() != null ? template.getCreatedDate() : "N/A"));
        dateLabel.setStyle("-fx-text-fill: #718096;");

        infoBox.getChildren().add(dateLabel);
        details.getChildren().add(infoBox);

        // Boutons d'action
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button previewBtn = new Button("👁 Aperçu");
        previewBtn.setStyle("-fx-padding: 8 15; -fx-font-size: 12; -fx-background-color: #0d47a1; -fx-text-fill: white;");
        previewBtn.setOnAction(e -> previewTemplate(template));

        Button editBtn = new Button("✏ Éditer");
        editBtn.setStyle("-fx-padding: 8 15; -fx-font-size: 12; -fx-background-color: #1b5e20; -fx-text-fill: white;");
        editBtn.setOnAction(e -> showEditTemplateDialog(template));

        Button deleteBtn = new Button("🗑 Supprimer");
        deleteBtn.setStyle("-fx-padding: 8 15; -fx-font-size: 12; -fx-background-color: #c62828; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteTemplate(template));

        actionBox.getChildren().addAll(previewBtn, editBtn, deleteBtn);

        card.getChildren().addAll(header, details, actionBox);
        return card;
    }

    private void previewTemplate(ContractTemplate template) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Aperçu du Template");
        dialog.initModality(Modality.APPLICATION_MODAL);

        DialogPane dialogPane = new DialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        VBox header = new VBox(5);
        header.setStyle("-fx-background-color: #0d47a1; -fx-padding: 20px;");
        Label headerLabel = new Label("📄 Aperçu du Template");
        headerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        header.getChildren().add(headerLabel);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label nameField = new Label("Nom: " + (template.getTemplateName() != null ? template.getTemplateName() : "N/A"));
        nameField.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label descField = new Label("Description:");
        descField.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #2c3e50;");

        TextArea contentArea = new TextArea(template.getTemplateContent() != null ? template.getTemplateContent() : "Aucun contenu");
        contentArea.setWrapText(true);
        contentArea.setPrefHeight(300);
        contentArea.setEditable(false);
        contentArea.setStyle("-fx-control-inner-background: #f5f5f5; -fx-font-size: 12;");

        content.getChildren().addAll(nameField, descField, contentArea);

        dialogPane.setContent(new VBox(header, content));
        dialogPane.getButtonTypes().add(ButtonType.OK);

        dialog.setDialogPane(dialogPane);
        dialog.showAndWait();
    }

    private void showCreateTemplateDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Créer un nouveau template");
        dialog.initModality(Modality.APPLICATION_MODAL);

        DialogPane dialogPane = new DialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        VBox header = new VBox(5);
        header.setStyle("-fx-background-color: #1b5e20; -fx-padding: 20px;");
        Label headerLabel = new Label("➕ Créer un Template");
        headerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        header.getChildren().add(headerLabel);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");

        TextField nameField = new TextField();
        nameField.setPromptText("Nom du template");
        nameField.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px;");

        TextArea descField = new TextArea();
        descField.setPromptText("Description du template");
        descField.setPrefHeight(100);
        descField.setWrapText(true);
        descField.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px;");

        TextArea contentField = new TextArea();
        contentField.setPromptText("Contenu du contrat (vous pouvez utiliser des variables comme {CLIENT_NAME}, {FREELANCER_NAME}, etc.)");
        contentField.setPrefHeight(250);
        contentField.setWrapText(true);
        contentField.setStyle("-fx-pref-width: 400px; -fx-font-size: 12px;");

        grid.add(createLabel("Nom:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(createLabel("Description:"), 0, 1);
        grid.add(descField, 1, 1);

        grid.add(createLabel("Contenu:"), 0, 2);
        grid.add(contentField, 1, 2);

        VBox mainContent = new VBox();
        mainContent.getChildren().addAll(header, grid);

        dialogPane.setContent(mainContent);

        ButtonType saveButton = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(saveButton, cancelButton);

        dialog.setDialogPane(dialogPane);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButton) {
                if (nameField.getText().trim().isEmpty()) {
                    showError("Validation", "Le nom du template est obligatoire");
                    return;
                }

                try {
                    ContractTemplate newTemplate = new ContractTemplate();
                    newTemplate.setTemplateName(nameField.getText().trim());
                    newTemplate.setDescription(descField.getText().trim());
                    newTemplate.setTemplateContent(contentField.getText().trim());
                    newTemplate.setCreatedDate(new java.sql.Timestamp(System.currentTimeMillis()));

                    templateService.createTemplate(newTemplate);
                    loadTemplates();
                    showSuccess("Le template a été créé avec succès");
                } catch (Exception e) {
                    showError("Erreur", "Impossible de créer le template: " + e.getMessage());
                }
            }
        });
    }

    private void showEditTemplateDialog(ContractTemplate template) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Éditer le template");
        dialog.initModality(Modality.APPLICATION_MODAL);

        DialogPane dialogPane = new DialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        VBox header = new VBox(5);
        header.setStyle("-fx-background-color: #1b5e20; -fx-padding: 20px;");
        Label headerLabel = new Label("✏ Éditer Template");
        headerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        header.getChildren().add(headerLabel);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");

        TextField nameField = new TextField(template.getTemplateName() != null ? template.getTemplateName() : "");
        nameField.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px;");

        TextArea descField = new TextArea(template.getDescription() != null ? template.getDescription() : "");
        descField.setPrefHeight(100);
        descField.setWrapText(true);
        descField.setStyle("-fx-pref-width: 400px; -fx-font-size: 13px;");

        TextArea contentField = new TextArea(template.getTemplateContent() != null ? template.getTemplateContent() : "");
        contentField.setPrefHeight(250);
        contentField.setWrapText(true);
        contentField.setStyle("-fx-pref-width: 400px; -fx-font-size: 12px;");

        grid.add(createLabel("Nom:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(createLabel("Description:"), 0, 1);
        grid.add(descField, 1, 1);

        grid.add(createLabel("Contenu:"), 0, 2);
        grid.add(contentField, 1, 2);

        VBox mainContent = new VBox();
        mainContent.getChildren().addAll(header, grid);

        dialogPane.setContent(mainContent);

        ButtonType saveButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(saveButton, cancelButton);

        dialog.setDialogPane(dialogPane);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButton) {
                try {
                    template.setTemplateName(nameField.getText().trim());
                    template.setDescription(descField.getText().trim());
                    template.setTemplateContent(contentField.getText().trim());
                    template.setUpdatedDate(new java.sql.Timestamp(System.currentTimeMillis()));

                    templateService.updateTemplate(template);
                    loadTemplates();
                    showSuccess("Le template a été modifié avec succès");
                } catch (Exception e) {
                    showError("Erreur", "Impossible de modifier le template: " + e.getMessage());
                }
            }
        });
    }

    private void deleteTemplate(ContractTemplate template) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le Template");
        confirm.setHeaderText("Êtes-vous sûr ?");
        confirm.setContentText("Cette action supprimera définitivement le template \"" + template.getTemplateName() + "\"");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    templateService.deleteTemplate(template.getIdTemplate());
                    loadTemplates();
                    showSuccess("Le template a été supprimé avec succès");
                } catch (Exception e) {
                    showError("Erreur", "Impossible de supprimer le template: " + e.getMessage());
                }
            }
        });
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        return label;
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
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

