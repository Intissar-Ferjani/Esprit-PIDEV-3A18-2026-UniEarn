package uniearn.controller.contracts.contrat;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uniearn.model.entities.contracts.ContractTemplate;
import uniearn.services.contracts.ContractTemplateService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Contrôleur pour la gestion des templates de contrats (Admin)
 */
public class ContractTemplateController {

    @FXML private TableView<ContractTemplate> templatesTable;
    @FXML private TableColumn<ContractTemplate, Integer> colId;
    @FXML private TableColumn<ContractTemplate, String> colName;
    @FXML private TableColumn<ContractTemplate, String> colDescription;
    @FXML private TableColumn<ContractTemplate, String> colCreatedDate;
    @FXML private TableColumn<ContractTemplate, Void> colActions;

    @FXML private Button btnNewTemplate;
    @FXML private Button btnRefresh;
    @FXML private Button btnDelete;
    @FXML private Label lblStats;
    @FXML private Label lblLastUpdate;

    private ContractTemplateService templateService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        templateService = new ContractTemplateService();
        setupTableColumns();
        loadTemplates();
        setupButtonListeners();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idTemplate"));
        colName.setCellValueFactory(new PropertyValueFactory<>("templateName"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCreatedDate.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                dateFormat.format(cellData.getValue().getCreatedDate())
            )
        );

        // Colonne Actions
        colActions.setCellFactory(param -> new TableCell<ContractTemplate, Void>() {
            private final Button btnEdit = new Button("✏️ Modifier");
            private final Button btnDelete = new Button("🗑️ Supprimer");

            {
                btnEdit.setStyle("-fx-padding: 5 10; -fx-font-size: 11;");
                btnDelete.setStyle("-fx-padding: 5 10; -fx-font-size: 11; -fx-text-fill: white; -fx-background-color: #F44336;");

                btnEdit.setOnAction(event -> {
                    ContractTemplate template = getTableView().getItems().get(getIndex());
                    editTemplate(template);
                });

                btnDelete.setOnAction(event -> {
                    ContractTemplate template = getTableView().getItems().get(getIndex());
                    deleteTemplate(template);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox(5);
                    hBox.getChildren().addAll(btnEdit, btnDelete);
                    setGraphic(hBox);
                }
            }
        });
    }

    private void setupButtonListeners() {
        btnNewTemplate.setOnAction(e -> openNewTemplateDialog());
        btnRefresh.setOnAction(e -> loadTemplates());
        btnDelete.setOnAction(e -> {
            ContractTemplate selected = templatesTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteTemplate(selected);
            } else {
                showAlert("Attention", "Veuillez sélectionner un template à supprimer", Alert.AlertType.WARNING);
            }
        });
    }

    private void loadTemplates() {
        List<ContractTemplate> templates = templateService.getAllTemplates();
        ObservableList<ContractTemplate> data = FXCollections.observableArrayList(templates);
        templatesTable.setItems(data);
        updateStats();
    }

    private void updateStats() {
        int total = templatesTable.getItems().size();
        lblStats.setText("Total: " + total + " templates");
        lblLastUpdate.setText("Dernière mise à jour: " + new SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
    }

    private void openNewTemplateDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contract_template_dialog.fxml"));
            Parent root = loader.load();

            ContractTemplateDialogController dialogController = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nouveau Template");
            dialogStage.setScene(new Scene(root, 700, 600));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnNewTemplate.getScene().getWindow());

            dialogController.setDialogStage(dialogStage);
            dialogController.setOnSave(template -> {
                if (templateService.createTemplate(template)) {
                    loadTemplates();
                    showAlert("Succès", "Template créé avec succès", Alert.AlertType.INFORMATION);
                    dialogStage.close();
                } else {
                    showAlert("Erreur", "Erreur lors de la création du template", Alert.AlertType.ERROR);
                }
            });

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ouverture du dialog", Alert.AlertType.ERROR);
        }
    }

    private void editTemplate(ContractTemplate template) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contract_template_dialog.fxml"));
            Parent root = loader.load();

            ContractTemplateDialogController dialogController = loader.getController();
            dialogController.setTemplate(template);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Template");
            dialogStage.setScene(new Scene(root, 700, 600));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnNewTemplate.getScene().getWindow());

            dialogController.setDialogStage(dialogStage);
            dialogController.setOnSave(updatedTemplate -> {
                updatedTemplate.setIdTemplate(template.getIdTemplate());
                if (templateService.updateTemplate(updatedTemplate)) {
                    loadTemplates();
                    showAlert("Succès", "Template modifié avec succès", Alert.AlertType.INFORMATION);
                    dialogStage.close();
                } else {
                    showAlert("Erreur", "Erreur lors de la modification", Alert.AlertType.ERROR);
                }
            });

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ouverture du dialog", Alert.AlertType.ERROR);
        }
    }

    private void deleteTemplate(ContractTemplate template) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer le template?");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer: " + template.getTemplateName() + "?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            if (templateService.deleteTemplate(template.getIdTemplate())) {
                loadTemplates();
                showAlert("Succès", "Template supprimé avec succès", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Erreur lors de la suppression", Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

